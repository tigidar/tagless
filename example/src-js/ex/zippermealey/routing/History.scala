package zippermealey.routing

import com.raquo.airstream.core.{EventStream, Signal, Observer}
import com.raquo.airstream.state.Var
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.eventbus.EventBus
import org.scalajs.dom
import org.scalajs.dom.{window, PopStateEvent}

/**
 * =============================================================================
 * BROWSER HISTORY INTEGRATION
 * =============================================================================
 * 
 * This module connects the routing system to the browser's History API,
 * enabling:
 *   1. URL updates when navigating programmatically
 *   2. Route updates when user clicks back/forward
 *   3. Deep linking (refreshing restores the route)
 *   4. Shareable URLs
 * 
 * =============================================================================
 * BROWSER HISTORY API PRIMER
 * =============================================================================
 * 
 * The browser provides:
 *   - window.location: Current URL
 *   - history.pushState(state, title, url): Add new history entry
 *   - history.replaceState(state, title, url): Replace current entry
 *   - history.back() / forward() / go(n): Navigate history
 *   - popstate event: Fired when user navigates history
 * 
 * Our integration:
 *   - Zipper navigation → pushState (new URL, new history entry)
 *   - Query/fragment changes → replaceState (same entry, updated URL)
 *   - popstate → Update zipper to match URL
 * 
 * =============================================================================
 * NAVIGATION MODES
 * =============================================================================
 * 
 * We support two URL modes:
 * 
 * 1. PATH MODE (recommended):
 *    /users/123/posts?filter=active#section
 *    - Clean URLs
 *    - Requires server configuration (all paths → index.html)
 * 
 * 2. HASH MODE (fallback):
 *    /#/users/123/posts?filter=active
 *    - Works without server config
 *    - Less clean but universally compatible
 */

/**
 * Navigation mode for URL handling.
 */
enum HistoryMode derives CanEqual:
  case Path  // Uses real paths: /users/123
  case Hash  // Uses hash: /#/users/123

/**
 * Commands for history manipulation.
 */
enum HistoryCommand derives CanEqual:
  /** Navigate to a new URI, adding a history entry */
  case Push(uri: Uri)

  /** Replace current URI without adding history entry */
  case Replace(uri: Uri)

  /** Go back n entries (negative to go forward) */
  case Go(delta: Int)

  /** Convenience for Go(-1) */
  case Back

  /** Convenience for Go(1) */
  case Forward

/**
 * The reactive history manager.
 * 
 * Provides:
 *   - currentUri: Signal of the current URI
 *   - navigate: Observer for navigation commands
 *   - Integration with RouteZipper
 */
trait History:
  /** Signal of the current URI */
  def currentUri: Signal[Uri]
  
  /** Navigate to a URI (push) */
  def push(uri: Uri): Unit
  
  /** Replace current URI */
  def replace(uri: Uri): Unit
  
  /** Go back */
  def back(): Unit
  
  /** Go forward */
  def forward(): Unit
  
  /** Observer for navigation commands */
  def commandObserver: Observer[HistoryCommand]

/**
 * Browser-based history implementation.
 */
final class BrowserHistory(
  mode: HistoryMode = HistoryMode.Path
)(using owner: Owner) extends History:
  
  // Internal state
  private val uriVar: Var[Uri] = Var(readCurrentUri())
  private val commandBus = new EventBus[HistoryCommand]
  
  // Listen to browser popstate events
  private val popstateListener: scala.scalajs.js.Function1[PopStateEvent, Unit] = { _ =>
    uriVar.set(readCurrentUri())
  }
  window.addEventListener("popstate", popstateListener)
  
  // Process navigation commands
  private val commandSubscription = commandBus.events.foreach {
    case HistoryCommand.Push(uri) => doPush(uri)
    case HistoryCommand.Replace(uri) => doReplace(uri)
    case HistoryCommand.Go(delta) => window.history.go(delta)
    case HistoryCommand.Back => window.history.back()
    case HistoryCommand.Forward => window.history.forward()
  }
  
  def currentUri: Signal[Uri] = uriVar.signal
  
  def push(uri: Uri): Unit = commandBus.writer.onNext(HistoryCommand.Push(uri))
  
  def replace(uri: Uri): Unit = commandBus.writer.onNext(HistoryCommand.Replace(uri))
  
  def back(): Unit = commandBus.writer.onNext(HistoryCommand.Back)
  
  def forward(): Unit = commandBus.writer.onNext(HistoryCommand.Forward)
  
  def commandObserver: Observer[HistoryCommand] = commandBus.writer
  
  // ===========================================================================
  // INTERNAL
  // ===========================================================================
  
  private def readCurrentUri(): Uri =
    val uriString = mode match
      case HistoryMode.Path =>
        val path = window.location.pathname
        val query = window.location.search
        val hash = window.location.hash
        s"$path$query$hash"
      case HistoryMode.Hash =>
        val hash = window.location.hash.stripPrefix("#")
        if hash.isEmpty then "/" else hash
    
    Uri.parse(uriString).getOrElse(Uri.root)
  
  private def uriToString(uri: Uri): String =
    mode match
      case HistoryMode.Path => uri.render
      case HistoryMode.Hash => s"#${uri.render}"
  
  private def doPush(uri: Uri): Unit =
    val urlString = uriToString(uri)
    window.history.pushState(null, "", urlString)
    uriVar.set(uri)
  
  private def doReplace(uri: Uri): Unit =
    val urlString = uriToString(uri)
    window.history.replaceState(null, "", urlString)
    uriVar.set(uri)

object BrowserHistory:
  def apply(mode: HistoryMode = HistoryMode.Path)(using Owner): BrowserHistory =
    new BrowserHistory(mode)

// =============================================================================
// ROUTER STORE - Combines Router, Zipper, and History
// =============================================================================

/**
 * The complete routing store that ties everything together:
 *   - Route tree (schema)
 *   - Route zipper (navigation state)
 *   - Browser history (URL sync)
 *   - Entity cache (ID lookups)
 * 
 * This is the main interface for the routing system.
 */
final class RouterStore[Page](
  router: Router[Page],
  history: History,
  loaderRegistry: LoaderRegistry = new DefaultLoaderRegistry
)(using owner: Owner):
  
  // ===========================================================================
  // STATE
  // ===========================================================================
  
  private val zipperVar: Var[RouteZipper[Page]] = Var(
    RouteZipper.fromUri(router.root, Uri.root).getOrElse(RouteZipper.fromRoot(router.root))
  )
  
  // Sync zipper with history
  private val historySyncSubscription = history.currentUri.foreach { uri =>
    RouteZipper.fromUri(router.root, uri).foreach(zipperVar.set)
  }
  
  // ===========================================================================
  // PUBLIC SIGNALS
  // ===========================================================================
  
  /** Current route zipper */
  val zipper: Signal[RouteZipper[Page]] = zipperVar.signal
  
  /** Current URI */
  val currentUri: Signal[Uri] = zipper.map(_.currentUri)
  
  /** Current page (if any) */
  val currentPage: Signal[Option[Page]] = zipper.map(_.currentPage)
  
  /** Current path parameters */
  val pathParams: Signal[Map[String, String]] = zipper.map(_.pathParams)
  
  /** Current query parameters */
  val query: Signal[Query] = zipper.map(_.query)
  
  /** Breadcrumb trail */
  val breadcrumbs: Signal[List[(String, Uri)]] = zipper.map(_.breadcrumbs)
  
  /** Current depth */
  val depth: Signal[Int] = zipper.map(_.depth)
  
  /** Route metadata */
  val meta: Signal[RouteMeta] = zipper.map(_.meta)
  
  // ===========================================================================
  // NAVIGATION COMMANDS
  // ===========================================================================
  
  /** Navigate to a URI */
  def navigateTo(uri: Uri): Unit =
    RouteZipper.fromUri(router.root, uri).foreach { z =>
      zipperVar.set(z)
      history.push(uri)
    }
  
  /** Navigate to a URI string */
  def navigateTo(uriString: String): Unit =
    Uri.parse(uriString).foreach(navigateTo)
  
  /** Go down to a child route */
  def goDown(segment: String): Unit =
    zipperVar.now().goDown(segment).foreach { z =>
      zipperVar.set(z)
      history.push(z.currentUri)
    }
  
  /** Go up to parent route */
  def goUp(): Unit =
    zipperVar.now().goUp.foreach { z =>
      zipperVar.set(z)
      history.push(z.currentUri)
    }
  
  /** Go to sibling route */
  def goLeft(): Unit =
    zipperVar.now().goLeft.foreach { z =>
      zipperVar.set(z)
      history.push(z.currentUri)
    }
  
  def goRight(): Unit =
    zipperVar.now().goRight.foreach { z =>
      zipperVar.set(z)
      history.push(z.currentUri)
    }
  
  /** Go to root */
  def goToRoot(): Unit =
    val z = zipperVar.now().goToRoot
    zipperVar.set(z)
    history.push(z.currentUri)
  
  /** Update query parameters (replaces history, doesn't push) */
  def setQuery(key: String, value: String): Unit =
    val z = zipperVar.now().setQuery(key, value)
    zipperVar.set(z)
    history.replace(z.currentUri)
  
  def removeQuery(key: String): Unit =
    val z = zipperVar.now().removeQuery(key)
    zipperVar.set(z)
    history.replace(z.currentUri)
  
  def clearQuery(): Unit =
    val z = zipperVar.now().clearQuery
    zipperVar.set(z)
    history.replace(z.currentUri)
  
  /** Update fragment (replaces history) */
  def setFragment(frag: String): Unit =
    val z = zipperVar.now().setFragment(frag)
    zipperVar.set(z)
    history.replace(z.currentUri)
  
  def clearFragment(): Unit =
    val z = zipperVar.now().clearFragment
    zipperVar.set(z)
    history.replace(z.currentUri)
  
  /** Browser history navigation */
  def back(): Unit = history.back()
  def forward(): Unit = history.forward()
  
  // ===========================================================================
  // ENTITY LOADING
  // ===========================================================================
  
  /** Register an entity loader */
  def registerLoader[K, V](name: String, loader: CachedLoader[K, V]): Unit =
    loaderRegistry.register(name, loader)
  
  /** Get a loader by name */
  def loader[K, V](name: String): Option[CachedLoader[K, V]] =
    loaderRegistry.get[K, V](name)
  
  // ===========================================================================
  // URL GENERATION
  // ===========================================================================
  
  /** Generate a URI for a page */
  def uriFor(page: Page, params: Map[String, String] = Map.empty): Option[Uri] =
    router.uriFor(page, params)
  
  /** Generate a URI for the current page with modified query */
  def currentUriWithQuery(params: (String, String)*): Uri =
    params.foldLeft(zipperVar.now().currentUri) { case (uri, (k, v)) =>
      uri.withQuery(k, v)
    }

object RouterStore:
  def apply[Page](
    routes: RouteNode[Page],
    mode: HistoryMode = HistoryMode.Path
  )(using Owner): RouterStore[Page] =
    val router = Router(routes)
    val history = BrowserHistory(mode)
    new RouterStore(router, history)

// =============================================================================
// LINK HELPERS
// =============================================================================

/**
 * Helpers for generating navigation links.
 */
object Link:
  /** Create a link data object */
  final case class LinkData(
    uri: Uri,
    isActive: Boolean,
    navigate: () => Unit
  ):
    def href: String = uri.render
  
  /** Create link data for a URI */
  def to[Page](store: RouterStore[Page], uri: Uri): Signal[LinkData] =
    store.currentUri.map { current =>
      LinkData(
        uri = uri,
        isActive = current.path == uri.path,
        navigate = () => store.navigateTo(uri)
      )
    }
  
  /** Create link data for a path string */
  def toPath[Page](store: RouterStore[Page], path: String): Signal[LinkData] =
    Uri.parse(path).map(uri => to(store, uri)).getOrElse {
      Signal.fromValue(LinkData(Uri.root, false, () => ()))
    }
