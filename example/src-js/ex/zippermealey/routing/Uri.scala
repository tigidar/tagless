package zippermealey.routing

import scala.util.{Try, Success, Failure}

/**
 * =============================================================================
 * URI AS AN ALGEBRAIC DATA TYPE
 * =============================================================================
 * 
 * URLs are traditionally treated as strings, leading to:
 *   - Parse errors at runtime
 *   - String manipulation bugs
 *   - No compile-time guarantees about structure
 * 
 * By modeling URIs as ADTs, we get:
 *   - Type-safe construction
 *   - Pattern matching for routing
 *   - Guaranteed well-formedness
 *   - Bidirectional (parse ↔ render)
 * 
 * =============================================================================
 * URI STRUCTURE
 * =============================================================================
 * 
 * A URI has the form:
 *   /path/segments?query=params&more=values#fragment
 *   \_____________/\______________________/\_______/
 *        Path              Query            Fragment
 * 
 * We decompose this into typed components that can be:
 *   - Constructed safely
 *   - Pattern matched
 *   - Transformed functionally
 */

// =============================================================================
// PATH SEGMENTS
// =============================================================================

/**
 * A single segment in a URI path.
 * 
 * We distinguish between:
 *   - Literal segments: "users", "posts" (fixed strings)
 *   - Captured segments: Values extracted from dynamic positions
 * 
 * At the SCHEMA level (route definition), we have patterns.
 * At the INSTANCE level (actual URL), we have resolved values.
 */
enum PathSegment derives CanEqual:
  /** A literal path segment like "users" or "settings" */
  case Literal(value: String)

  /** A captured dynamic segment with its parameter name and value */
  case Captured(paramName: String, value: String)
  
  /** Render this segment to a string */
  def render: String = this match
    case Literal(v) => v
    case Captured(_, v) => v
  
  /** Check if this matches a pattern segment */
  def matches(pattern: SegmentPattern): Option[Map[String, String]] = 
    (this, pattern) match
      case (Literal(v), SegmentPattern.Literal(p)) if v == p => 
        Some(Map.empty)
      case (Literal(v), SegmentPattern.Param(name)) => 
        Some(Map(name -> v))
      case (Captured(_, v), SegmentPattern.Param(name)) => 
        Some(Map(name -> v))
      case (_, SegmentPattern.Wildcard) => 
        Some(Map.empty)
      case _ => 
        None

/**
 * A pattern for matching path segments (used in route definitions).
 */
enum SegmentPattern derives CanEqual:
  /** Match a literal string exactly */
  case Literal(value: String)

  /** Capture a dynamic value with the given parameter name */
  case Param(name: String)

  /** Match anything */
  case Wildcard
  
  /** Render as a pattern string (for debugging/display) */
  def render: String = this match
    case Literal(v) => v
    case Param(name) => s":$name"
    case Wildcard => "*"

// =============================================================================
// PATH (Sequence of Segments)
// =============================================================================

/**
 * A complete URI path - a sequence of segments.
 * 
 * Examples:
 *   /users/123/posts  →  Path(List(Literal("users"), Captured("userId", "123"), Literal("posts")))
 *   /                 →  Path(List.empty)
 */
final case class Path(segments: List[PathSegment]):
  
  /** Is this the root path? */
  def isRoot: Boolean = segments.isEmpty
  
  /** Number of segments */
  def length: Int = segments.length
  
  /** Append a segment */
  def /(segment: PathSegment): Path = Path(segments :+ segment)
  
  /** Append a literal segment */
  def /(literal: String): Path = this / PathSegment.Literal(literal)
  
  /** Get all captured parameters */
  def params: Map[String, String] = 
    segments.collect { case PathSegment.Captured(name, value) => name -> value }.toMap
  
  /** Render to URL path string */
  def render: String = 
    if segments.isEmpty then "/"
    else "/" + segments.map(_.render).mkString("/")
  
  /** Get parent path (drop last segment) */
  def parent: Option[Path] = 
    if segments.isEmpty then None
    else Some(Path(segments.init))
  
  /** Get the last segment */
  def last: Option[PathSegment] = segments.lastOption
  
  /** Take first n segments */
  def take(n: Int): Path = Path(segments.take(n))
  
  /** Drop first n segments */
  def drop(n: Int): Path = Path(segments.drop(n))

object Path:
  /** The root path */
  val root: Path = Path(Nil)

  /** Create from a list of literal strings */
  def apply(segments: String*): Path =
    Path(segments.toList.map(PathSegment.Literal(_)))

  given CanEqual[Path, Path] = CanEqual.derived
  
  /** Parse a path string */
  def parse(pathString: String): Either[ParseError, Path] =
    val cleaned = pathString.stripPrefix("/").stripSuffix("/")
    if cleaned.isEmpty then Right(root)
    else
      val segments = cleaned.split("/").toList.map { s =>
        PathSegment.Literal(java.net.URLDecoder.decode(s, "UTF-8"))
      }
      Right(Path(segments))

// =============================================================================
// QUERY PARAMETERS
// =============================================================================

/**
 * Query parameters - the ?key=value&key2=value2 part.
 * 
 * Design decisions:
 *   - Keys can have multiple values (standard URI semantics)
 *   - Values are optional (key-only params are valid)
 *   - Order is preserved for reproducibility
 * 
 * Query params represent FILTERING and EPHEMERAL STATE:
 *   - ?filter=active&sort=date  (filtering)
 *   - ?page=3                   (pagination state)
 *   - ?modal=settings           (UI state that should be shareable)
 */
final case class Query(params: List[(String, Option[String])]):
  
  def isEmpty: Boolean = params.isEmpty
  
  def nonEmpty: Boolean = params.nonEmpty
  
  /** Get first value for a key */
  def get(key: String): Option[String] = 
    params.collectFirst { case (k, v) if k == key => v }.flatten
  
  /** Get all values for a key */
  def getAll(key: String): List[String] = 
    params.collect { case (k, Some(v)) if k == key => v }
  
  /** Check if a key exists (even without value) */
  def has(key: String): Boolean = 
    params.exists(_._1 == key)
  
  /** Add or replace a parameter */
  def set(key: String, value: String): Query =
    Query(params.filterNot(_._1 == key) :+ (key -> Some(value)))
  
  /** Add a parameter (allows multiple values) */
  def add(key: String, value: String): Query =
    Query(params :+ (key -> Some(value)))
  
  /** Remove all values for a key */
  def remove(key: String): Query =
    Query(params.filterNot(_._1 == key))
  
  /** Convert to a simple map (last value wins for duplicates) */
  def toMap: Map[String, String] =
    params.collect { case (k, Some(v)) => k -> v }.toMap
  
  /** Render to query string (without leading ?) */
  def render: String =
    if params.isEmpty then ""
    else params.map {
      case (k, Some(v)) => s"${encode(k)}=${encode(v)}"
      case (k, None) => encode(k)
    }.mkString("&")
  
  private def encode(s: String): String = 
    java.net.URLEncoder.encode(s, "UTF-8")

object Query:
  val empty: Query = Query(Nil)
  
  def apply(params: (String, String)*): Query =
    Query(params.toList.map { case (k, v) => k -> Some(v) })
  
  def parse(queryString: String): Either[ParseError, Query] =
    if queryString.isEmpty then Right(empty)
    else
      val params = queryString.stripPrefix("?").split("&").toList.map { param =>
        param.split("=", 2) match
          case Array(k, v) => decode(k) -> Some(decode(v))
          case Array(k) => decode(k) -> None
      }
      Right(Query(params))
  
  private def decode(s: String): String =
    java.net.URLDecoder.decode(s, "UTF-8")

// =============================================================================
// FRAGMENT
// =============================================================================

/**
 * URI fragment - the #anchor part.
 * 
 * Used for:
 *   - In-page navigation (#section)
 *   - Client-side state that shouldn't hit server
 */
final case class Fragment(value: String):
  def render: String = value

object Fragment:
  def parse(fragmentString: String): Either[ParseError, Fragment] =
    Right(Fragment(fragmentString.stripPrefix("#")))

// =============================================================================
// COMPLETE URI
// =============================================================================

/**
 * A complete URI combining path, query, and fragment.
 * 
 * This is the full ADT representation of a URL, suitable for:
 *   - Routing decisions
 *   - Browser history state
 *   - Link generation
 *   - State serialization
 */
final case class Uri(
  path: Path,
  query: Query = Query.empty,
  fragment: Option[Fragment] = None
):
  /** All captured path parameters */
  def pathParams: Map[String, String] = path.params
  
  /** Combined path + query params */
  def allParams: Map[String, String] = pathParams ++ query.toMap
  
  /** Render to full URI string */
  def render: String =
    val pathStr = path.render
    val queryStr = if query.isEmpty then "" else s"?${query.render}"
    val fragStr = fragment.map(f => s"#${f.render}").getOrElse("")
    s"$pathStr$queryStr$fragStr"
  
  /** Navigate to a child path segment */
  def /(segment: String): Uri = copy(path = path / segment)
  
  /** Navigate to a child with a captured parameter */
  def /[A](param: (String, A))(using show: Show[A]): Uri =
    copy(path = path / PathSegment.Captured(param._1, show.show(param._2)))
  
  /** Add/update a query parameter */
  def withQuery(key: String, value: String): Uri =
    copy(query = query.set(key, value))
  
  /** Add multiple query parameters */
  def withQueryParams(params: (String, String)*): Uri =
    params.foldLeft(this) { case (uri, (k, v)) => uri.withQuery(k, v) }
  
  /** Set fragment */
  def withFragment(frag: String): Uri =
    copy(fragment = Some(Fragment(frag)))
  
  /** Remove query params */
  def withoutQuery: Uri = copy(query = Query.empty)
  
  /** Remove fragment */
  def withoutFragment: Uri = copy(fragment = None)
  
  /** Get parent URI (one level up in path) */
  def parent: Option[Uri] = path.parent.map(p => copy(path = p))

object Uri:
  /** Root URI */
  val root: Uri = Uri(Path.root)
  
  /** Create from path segments */
  def apply(segments: String*): Uri = Uri(Path(segments*))
  
  /** Parse a complete URI string */
  def parse(uriString: String): Either[ParseError, Uri] =
    // Split into path, query, fragment
    val (pathAndQuery, fragmentStr) = uriString.split("#", 2) match
      case Array(pq, f) => (pq, Some(f))
      case Array(pq) => (pq, None)

    val (pathStr, queryStr) = pathAndQuery.split("\\?", 2) match
      case Array(p, q) => (p, Some(q))
      case Array(p) => (p, None)

    for
      path <- Path.parse(pathStr)
      query <- queryStr.map(Query.parse).getOrElse(Right(Query.empty))
      frag <- fragmentStr match
        case Some(f) => Fragment.parse(f).map(Some(_))
        case None => Right(None)
    yield Uri(path, query, frag)

// =============================================================================
// PARSE ERRORS
// =============================================================================

enum ParseError:
  case InvalidPath(message: String)
  case InvalidQuery(message: String)
  case InvalidFragment(message: String)
  case InvalidUri(message: String)

// =============================================================================
// TYPE CLASS FOR SHOWING VALUES IN URLS
// =============================================================================

/**
 * Type class for converting values to URL-safe strings.
 */
trait Show[A]:
  def show(a: A): String

object Show:
  given Show[String] = identity(_)
  given Show[Int] = _.toString
  given Show[Long] = _.toString
  given Show[java.util.UUID] = _.toString
  
  def apply[A](using s: Show[A]): Show[A] = s

/**
 * Type class for parsing values from URL strings.
 */
trait Read[A]:
  def read(s: String): Either[String, A]

object Read:
  given Read[String] = s => Right(s)
  given Read[Int] = s => Try(s.toInt).toEither.left.map(_.getMessage)
  given Read[Long] = s => Try(s.toLong).toEither.left.map(_.getMessage)
  given Read[java.util.UUID] = s => Try(java.util.UUID.fromString(s)).toEither.left.map(_.getMessage)
  
  def apply[A](using r: Read[A]): Read[A] = r
