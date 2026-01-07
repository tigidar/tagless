package zippermealey.routing

import scala.concurrent.duration.*
import scala.collection.mutable

/**
 * =============================================================================
 * ENTITY CACHE - Cached ID Lookups with TTL
 * =============================================================================
 * 
 * URLs contain IDs, not full entities. When we navigate to /users/123, we need
 * to fetch the User with ID 123. This cache:
 * 
 *   1. Stores fetched entities by ID
 *   2. Automatically expires entries after TTL
 *   3. Provides async fetch with deduplication
 *   4. Integrates with the router for automatic prefetching
 * 
 * =============================================================================
 * WHY ID-BASED, NOT JSON?
 * =============================================================================
 * 
 * URLs should contain IDs, not serialized data, because:
 * 
 *   1. URLs have length limits (~2000 chars practical)
 *   2. IDs are stable, JSON changes with schema
 *   3. IDs are opaque - no security concerns about exposing data
 *   4. Server can validate/authorize ID access
 *   5. Caching works naturally with IDs as keys
 * 
 * The pattern is:
 *   URL: /users/123           (ID in URL)
 *   Cache: 123 → User(...)    (Entity in cache)
 *   Fetch: ID → Entity        (Loader function)
 */

/**
 * A cached entry with metadata.
 */
final case class CacheEntry[A](
  value: A,
  fetchedAt: Long,
  ttlMillis: Long
):
  def isExpired(now: Long): Boolean = 
    now > fetchedAt + ttlMillis
  
  def remainingTtl(now: Long): Long =
    math.max(0, (fetchedAt + ttlMillis) - now)

/**
 * Cache statistics for monitoring.
 */
final case class CacheStats(
  hits: Long,
  misses: Long,
  evictions: Long,
  size: Int
):
  def hitRate: Double = 
    if hits + misses == 0 then 0.0
    else hits.toDouble / (hits + misses)

/**
 * A type-safe entity cache with TTL.
 * 
 * @tparam K The key type (typically an ID type)
 * @tparam V The value type (the entity)
 */
trait EntityCache[K, V]:
  /** Get an entry if present and not expired */
  def get(key: K): Option[V]
  
  /** Put an entry with the default TTL */
  def put(key: K, value: V): Unit
  
  /** Put an entry with a specific TTL */
  def put(key: K, value: V, ttl: FiniteDuration): Unit
  
  /** Remove an entry */
  def remove(key: K): Option[V]
  
  /** Clear all entries */
  def clear(): Unit
  
  /** Get cache statistics */
  def stats: CacheStats
  
  /** Check if key exists (not expired) */
  def contains(key: K): Boolean = get(key).isDefined

/**
 * In-memory cache implementation with TTL.
 */
final class InMemoryCache[K, V](
  defaultTtl: FiniteDuration,
  maxSize: Int = 1000,
  clock: () => Long = () => System.currentTimeMillis()
) extends EntityCache[K, V]:
  
  private val entries = mutable.LinkedHashMap.empty[K, CacheEntry[V]]
  private var _hits = 0L
  private var _misses = 0L
  private var _evictions = 0L
  
  def get(key: K): Option[V] = synchronized {
    entries.get(key) match
      case Some(entry) if !entry.isExpired(clock()) =>
        _hits += 1
        // Move to end (LRU)
        val _ = entries.remove(key)
        val _ = entries.put(key, entry)
        Some(entry.value)
      case Some(_) =>
        // Expired
        val _ = entries.remove(key)
        _misses += 1
        None
      case None =>
        _misses += 1
        None
  }
  
  def put(key: K, value: V): Unit = 
    put(key, value, defaultTtl)
  
  def put(key: K, value: V, ttl: FiniteDuration): Unit = synchronized {
    // Evict if at capacity
    while entries.size >= maxSize do
      entries.headOption.foreach { case (k, _) =>
        val _ = entries.remove(k)
        _evictions += 1
      }

    val entry = CacheEntry(value, clock(), ttl.toMillis)
    val _ = entries.put(key, entry)
  }
  
  def remove(key: K): Option[V] = synchronized {
    entries.remove(key).map(_.value)
  }
  
  def clear(): Unit = synchronized {
    entries.clear()
  }
  
  def stats: CacheStats = synchronized {
    // Clean expired entries
    val now = clock()
    val expired = entries.filter(_._2.isExpired(now)).keys.toList
    expired.foreach(entries.remove)
    
    CacheStats(_hits, _misses, _evictions, entries.size)
  }
  
  /** Get all non-expired entries (for debugging) */
  def snapshot: Map[K, V] = synchronized {
    val now = clock()
    entries.filter(!_._2.isExpired(now)).view.mapValues(_.value).toMap
  }

object EntityCache:
  /** Create a simple in-memory cache */
  def inMemory[K, V](
    ttl: FiniteDuration = 5.minutes,
    maxSize: Int = 1000
  ): EntityCache[K, V] =
    new InMemoryCache[K, V](ttl, maxSize)

// =============================================================================
// CACHED LOADER - Async fetch with caching
// =============================================================================

import scala.concurrent.{Future, ExecutionContext}

/**
 * A loader that fetches entities by ID with automatic caching.
 * 
 * Features:
 *   - Deduplicates concurrent requests for the same ID
 *   - Caches results with TTL
 *   - Provides batch loading for efficiency
 */
trait CachedLoader[K, V]:
  /** Load a single entity (from cache or fetch) */
  def load(key: K)(using ExecutionContext): Future[Option[V]]
  
  /** Load multiple entities */
  def loadMany(keys: Set[K])(using ExecutionContext): Future[Map[K, V]]
  
  /** Invalidate a cached entry */
  def invalidate(key: K): Unit
  
  /** Invalidate all entries */
  def invalidateAll(): Unit
  
  /** Get cache stats */
  def stats: CacheStats

/**
 * Default implementation of CachedLoader.
 */
final class DefaultCachedLoader[K, V](
  cache: EntityCache[K, V],
  fetcher: K => Future[Option[V]],
  batchFetcher: Option[Set[K] => Future[Map[K, V]]] = None
)(using ec: ExecutionContext) extends CachedLoader[K, V]:
  
  // Track in-flight requests to deduplicate
  private val inFlight = mutable.Map.empty[K, Future[Option[V]]]
  
  def load(key: K)(using ExecutionContext): Future[Option[V]] = synchronized {
    // Check cache first
    cache.get(key) match
      case Some(value) => Future.successful(Some(value))
      case None =>
        // Check if already fetching
        inFlight.get(key) match
          case Some(future) => future
          case None =>
            // Start new fetch
            val future = fetcher(key).map { result =>
              result.foreach(v => cache.put(key, v))
              synchronized { val _ = inFlight.remove(key) }
              result
            }.recover { case _ =>
              synchronized { val _ = inFlight.remove(key) }
              None
            }
            val _ = inFlight.put(key, future)
            future
  }
  
  def loadMany(keys: Set[K])(using ExecutionContext): Future[Map[K, V]] =
    // Separate cached from uncached
    val (cached, uncached) = keys.partition(cache.contains)
    val cachedResults = cached.flatMap(k => cache.get(k).map(k -> _)).toMap
    
    if uncached.isEmpty then Future.successful(cachedResults)
    else
      // Use batch fetcher if available, otherwise individual fetches
      val fetchFuture = batchFetcher match
        case Some(bf) => bf(uncached)
        case None => 
          Future.sequence(uncached.map(k => load(k).map(v => k -> v)))
            .map(_.collect { case (k, Some(v)) => k -> v }.toMap)
      
      fetchFuture.map { fetched =>
        fetched.foreach { case (k, v) => cache.put(k, v) }
        cachedResults ++ fetched
      }
  
  def invalidate(key: K): Unit =
    val _ = cache.remove(key)
  
  def invalidateAll(): Unit = cache.clear()
  
  def stats: CacheStats = cache.stats

object CachedLoader:
  def apply[K, V](
    fetcher: K => Future[Option[V]],
    ttl: FiniteDuration = 5.minutes,
    maxSize: Int = 1000
  )(using ExecutionContext): CachedLoader[K, V] =
    val cache = EntityCache.inMemory[K, V](ttl, maxSize)
    new DefaultCachedLoader(cache, fetcher)
  
  def withBatch[K, V](
    fetcher: K => Future[Option[V]],
    batchFetcher: Set[K] => Future[Map[K, V]],
    ttl: FiniteDuration = 5.minutes,
    maxSize: Int = 1000
  )(using ExecutionContext): CachedLoader[K, V] =
    val cache = EntityCache.inMemory[K, V](ttl, maxSize)
    new DefaultCachedLoader(cache, fetcher, Some(batchFetcher))

// =============================================================================
// ROUTE CACHE INTEGRATION
// =============================================================================

/**
 * Registry of entity loaders by type.
 * 
 * This allows the router to know how to fetch entities referenced by IDs in URLs.
 */
trait LoaderRegistry:
  def register[K, V](name: String, loader: CachedLoader[K, V]): Unit
  def get[K, V](name: String): Option[CachedLoader[K, V]]

final class DefaultLoaderRegistry extends LoaderRegistry:
  private val loaders = mutable.Map.empty[String, CachedLoader[?, ?]]

  def register[K, V](name: String, loader: CachedLoader[K, V]): Unit =
    val _ = loaders.put(name, loader)

  def get[K, V](name: String): Option[CachedLoader[K, V]] =
    loaders.get(name).map(_.asInstanceOf[CachedLoader[K, V]])

/**
 * Hints for route-level caching.
 */
final case class CacheHint(
  paramName: String,
  loaderName: String,
  prefetch: Boolean = true
)

/**
 * Extension to RouteMeta for cache configuration.
 */
extension (meta: RouteMeta)
  def withCacheHints(hints: CacheHint*): RouteMeta =
    meta.copy(tags = meta.tags ++ hints.map(h => s"cache:${h.paramName}:${h.loaderName}"))
