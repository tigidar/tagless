package zippermealey.routing

/** \=============================================================================
  * ROUTE TREE - THE SCHEMA OF YOUR APPLICATION
  * \=============================================================================
  *
  * A Route Tree defines the SHAPE of your application's navigation structure.
  * Think of it as a blueprint that says "these are the valid paths through my
  * app."
  *
  * KEY INSIGHT: The route tree has LABELED EDGES, not labeled nodes.
  *
  * ┌─────┐ │ App │ (root node) └──┬──┘ ┌───────────┬──┴───┬────────────┐
  * "users" "posts" "about" "settings" ← edge labels │ │ │ │ ┌───┴───┐ ┌───┴───┐
  * [page] ┌───┴───┐ │ Users │ │ Posts │ │ Settings│ └───┬───┘ └───┬───┘
  * └─────────┘ ":id" ":id" ← parameterized edges │ │ ┌───┴────┐ ┌───┴────┐
  * │UserPage│ │PostPage│ └────────┘ └────────┘
  *
  * This maps directly to URLs: /users → Users node /users/123 → UserPage node
  * (with id=123 captured) /posts/456 → PostPage node (with id=456 captured)
  *
  * \=============================================================================
  * WHY LABELED EDGES?
  * \=============================================================================
  *
  * Labeled edges (vs. labeled nodes) are better because:
  *
  *   1. They match URL semantics: /users/123/posts/456 Each segment is a "step"
  *      (edge), not a "place" (node)
  *
  * 2. The same node type can be reached via different paths:
  * /users/123/posts/456 ← PostPage /posts/456 ← PostPage (same page, different
  * context!)
  *
  * 3. Navigation IS traversing edges, nodes are just waypoints
  *
  * 4. Query params and fragments attach to nodes (the current position), while
  * path segments ARE the edges you traversed to get there.
  */

// =============================================================================
// ROUTE NODE - What you find at each position in the tree
// =============================================================================

/** A node in the route tree.
  *
  * @tparam Page
  *   The type representing what page/component to render
  *
  * @param page
  *   What to render at this location (None for intermediate nodes)
  * @param children
  *   Labeled edges to child nodes
  * @param queryCodec
  *   How to decode query params at this node
  * @param meta
  *   Additional metadata (title, auth requirements, etc.)
  */
final case class RouteNode[+Page](
    page: Option[Page],
    children: Map[EdgeLabel, RouteNode[Page]],
    queryCodec: Option[QueryCodec[?]] = None,
    meta: RouteMeta = RouteMeta.empty
):
  /** Is this a leaf node (no children)? */
  def isLeaf: Boolean = children.isEmpty

  /** Does this node render a page? */
  def hasPage: Boolean = page.isDefined

  /** Find child by literal edge */
  def child(label: String): Option[RouteNode[Page]] =
    children.get(EdgeLabel.Literal(label))

  /** Find child by any matching edge (including params) */
  def matchChild(
      segment: String
  ): Option[(EdgeLabel, RouteNode[Page], Map[String, String])] =
    // Try literal match first (more specific)
    children.get(EdgeLabel.Literal(segment)) match
      case Some(node) => Some((EdgeLabel.Literal(segment), node, Map.empty))
      case None       =>
        // Try param matches
        children.collectFirst {
          case (label @ EdgeLabel.Param(name, validator), node)
              if validator.forall(_.isValid(segment)) =>
            (label, node, Map(name -> segment))
          case (label @ EdgeLabel.Wildcard, node) =>
            (label, node, Map.empty)
        }

  /** Get all edge labels */
  def edgeLabels: Set[EdgeLabel] = children.keySet

  /** Map over pages in this tree */
  def map[B](f: Page => B): RouteNode[B] =
    RouteNode(
      page.map(f),
      children.view.mapValues(_.map(f)).toMap,
      queryCodec,
      meta
    )

object RouteNode:
  /** Create an empty intermediate node */
  def empty[Page]: RouteNode[Page] = RouteNode(None, Map.empty)

  /** Create a leaf node with a page */
  def leaf[Page](page: Page): RouteNode[Page] = RouteNode(Some(page), Map.empty)

  /** Create an intermediate node with a page (renders AND has children) */
  def branch[Page](
      page: Page,
      children: Map[EdgeLabel, RouteNode[Page]]
  ): RouteNode[Page] =
    RouteNode(Some(page), children)

// =============================================================================
// EDGE LABELS - How you move between nodes
// =============================================================================

/** An edge label defines what path segment(s) traverse this edge.
  *
  * This is the heart of URL → Route matching.
  */
enum EdgeLabel:
  /** A literal segment - matches exactly. Example: "users" matches only the
    * segment "users"
    */
  case Literal(value: String)

  /** A parameter segment - captures a value. Example: Param("userId") matches
    * any segment, captures it as "userId"
    *
    * @param name
    *   The parameter name (used in captured params)
    * @param validator
    *   Optional validation (e.g., must be numeric)
    */
  case Param(name: String, validator: Option[ParamValidator] = None)

  /** A wildcard - matches anything (catch-all). Used for 404 handlers or
    * flexible routing.
    */
  case Wildcard

  /** Priority for matching (higher = match first) */
  def priority: Int = this match
    case Literal(_)        => 3 // Most specific
    case Param(_, Some(_)) => 2 // Param with validation
    case Param(_, None)    => 1 // Param without validation
    case Wildcard          => 0 // Least specific (catch-all)

  /** Render as a pattern string */
  def render: String = this match
    case Literal(v)     => v
    case Param(name, _) => s":$name"
    case Wildcard       => "*"

object EdgeLabel:
  /** Ordering by priority (highest first) */
  given Ordering[EdgeLabel] = Ordering.by(-_.priority)

/** Validates parameter values.
  */
trait ParamValidator:
  def isValid(value: String): Boolean
  def description: String

object ParamValidator:
  /** Numeric values only */
  val numeric: ParamValidator = new ParamValidator:
    def isValid(value: String) = value.forall(_.isDigit)
    def description = "numeric"

  /** UUID format */
  val uuid: ParamValidator = new ParamValidator:
    def isValid(value: String) =
      try { java.util.UUID.fromString(value); true }
      catch { case _: Exception => false }
    def description = "uuid"

  /** Alphanumeric only */
  val alphanumeric: ParamValidator = new ParamValidator:
    def isValid(value: String) = value.forall(_.isLetterOrDigit)
    def description = "alphanumeric"

  /** Custom regex */
  def pattern(regex: String): ParamValidator = new ParamValidator:
    private val compiled = regex.r
    def isValid(value: String) = compiled.matches(value)
    def description = s"pattern($regex)"

// =============================================================================
// ROUTE METADATA
// =============================================================================

/** Metadata attached to route nodes.
  *
  * This is where you put:
  *   - Page titles
  *   - Authentication requirements
  *   - Caching hints
  *   - Analytics tags
  */
final case class RouteMeta(
    title: Option[String] = None,
    requiresAuth: Boolean = false,
    cachePolicy: CachePolicy = CachePolicy.Default,
    tags: Set[String] = Set.empty
)

object RouteMeta:
  val empty: RouteMeta = RouteMeta()

enum CachePolicy:
  case Default
  case NoCache
  case CacheFor(seconds: Int)
  case CacheWithRevalidation

// =============================================================================
// QUERY CODEC - Type-safe query parameter handling
// =============================================================================

/** Codec for encoding/decoding query parameters to/from typed values.
  *
  * This is how ?filter=active&sort=name becomes a typed Filter case class.
  */
trait QueryCodec[A]:
  def decode(query: Query): Either[String, A]
  def encode(value: A): Query

object QueryCodec:
  /** Codec for a single required parameter */
  def param[A](
      name: String
  )(using read: Read[A], show: Show[A]): QueryCodec[A] =
    new QueryCodec[A]:
      def decode(query: Query) =
        query
          .get(name)
          .toRight(s"Missing required parameter: $name")
          .flatMap(read.read)
      def encode(value: A) = Query(name -> show.show(value))

  /** Codec for a single optional parameter */
  def optParam[A](
      name: String
  )(using read: Read[A], show: Show[A]): QueryCodec[Option[A]] =
    new QueryCodec[Option[A]]:
      def decode(query: Query) =
        query.get(name) match
          case None    => Right(None)
          case Some(s) => read.read(s).map(Some(_))
      def encode(value: Option[A]) =
        value.map(v => Query(name -> show.show(v))).getOrElse(Query.empty)

  /** Combine two codecs into a tuple */
  def product[A, B](ca: QueryCodec[A], cb: QueryCodec[B]): QueryCodec[(A, B)] =
    new QueryCodec[(A, B)]:
      def decode(query: Query) =
        for
          a <- ca.decode(query)
          b <- cb.decode(query)
        yield (a, b)
      def encode(value: (A, B)) =
        val qa = ca.encode(value._1)
        val qb = cb.encode(value._2)
        Query(qa.params ++ qb.params)

// =============================================================================
// MATCHED ROUTE - Result of routing
// =============================================================================

/** A successfully matched route.
  *
  * Contains everything needed to:
  *   - Render the page
  *   - Display breadcrumbs
  *   - Generate related URLs
  *   - Populate caches
  */
final case class MatchedRoute[+Page](
    /** The matched node */
    node: RouteNode[Page],
    /** The page to render (from the node) */
    page: Page,
    /** All captured path parameters */
    pathParams: Map[String, String],
    /** Parsed query parameters */
    query: Query,
    /** The path that was matched */
    path: Path,
    /** The traversed edges (for breadcrumbs) */
    breadcrumbs: List[(EdgeLabel, RouteNode[Page])],
    /** Optional fragment */
    fragment: Option[Fragment]
):
  /** Get a specific path parameter */
  def param(name: String): Option[String] = pathParams.get(name)

  /** Get a typed path parameter */
  def paramAs[A](name: String)(using read: Read[A]): Either[String, A] =
    pathParams
      .get(name)
      .toRight(s"Parameter not found: $name")
      .flatMap(read.read)

  /** Render breadcrumbs as a list of (label, uri) */
  def breadcrumbLinks: List[(String, Uri)] =
    breadcrumbs
      .scanLeft((Path.root, "Root")) { case ((path, _), (edge, _)) =>
        val newPath = path / edge.render
        (newPath, edge.render)
      }
      .map { case (path, label) => (label, Uri(path)) }

/** Result of attempting to match a URI to a route.
  */
enum RouteResult[+Page]:
  case Matched(route: MatchedRoute[Page])
  case NotFound(uri: Uri, partialMatch: Option[RouteNode[Page]])
  case Redirect(from: Uri, to: Uri)
