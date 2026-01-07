package zippermealey.routing

/** \=============================================================================
  * ROUTE ZIPPER - Navigating the Route Tree with Context
  * \=============================================================================
  *
  * The Route Zipper combines:
  *   1. Huet's Zipper concept (focus + context) 2. URL/URI as the "address" of
  *      the focus 3. Browser history integration 4. Type-safe parameter
  *      accumulation
  *
  * \=============================================================================
  * KEY INSIGHT: URL IS A SERIALIZED ZIPPER PATH
  * \=============================================================================
  *
  * A URL like /users/123/posts/456 is really a sequence of navigation commands:
  *   - Start at root
  *   - Go down edge "users"
  *   - Go down edge ":userId" (capturing 123)
  *   - Go down edge "posts"
  *   - Go down edge ":postId" (capturing 456)
  *
  * The zipper context tracks this path, making navigation bidirectional:
  *   - Forward: URI → Zipper position
  *   - Backward: Zipper position → URI
  *
  * \=============================================================================
  * CONTEXT STRUCTURE
  * \=============================================================================
  *
  * At each level, the context remembers:
  *   - The edge label we traversed
  *   - The captured parameter (if any)
  *   - The parent node
  *   - Sibling edges (for left/right navigation)
  */

/** A single "crumb" of context - one level of navigation.
  *
  * @param edge
  *   The edge label we traversed to get here
  * @param capturedValue
  *   If edge was a Param, the captured value
  * @param parentNode
  *   The node we came from
  * @param leftSiblings
  *   Edge labels to the left (for sibling navigation)
  * @param rightSiblings
  *   Edge labels to the right
  */
final case class RouteCrumb[+Page](
    edge: EdgeLabel,
    capturedValue: Option[String],
    parentNode: RouteNode[Page],
    leftSiblings: List[EdgeLabel],
    rightSiblings: List[EdgeLabel]
):
  /** The segment this crumb represents in the URL */
  def segment: String = capturedValue.getOrElse(edge.render)

type RouteContext[Page] = List[RouteCrumb[Page]]

/** The Route Zipper - a position in the route tree with full context.
  *
  * This is the central data structure for navigation. It maintains:
  *   - Current focus (the node we're "at")
  *   - Context (the path we took to get here)
  *   - Accumulated parameters
  *   - Query state
  *   - Fragment state
  */
final case class RouteZipper[+Page](
    focus: RouteNode[Page],
    context: RouteContext[Page],
    query: Query = Query.empty,
    fragment: Option[Fragment] = None
):
  // ===========================================================================
  // QUERIES
  // ===========================================================================

  /** The current path (reconstructed from context) */
  def currentPath: Path =
    Path(context.reverse.map(crumb => PathSegment.Literal(crumb.segment)))

  /** The current URI (path + query + fragment) */
  def currentUri: Uri = Uri(currentPath, query, fragment)

  /** All accumulated path parameters */
  def pathParams: Map[String, String] =
    context.collect {
      case RouteCrumb(EdgeLabel.Param(name, _), Some(value), _, _, _) =>
        name -> value
    }.toMap

  /** Combined path + query params */
  def allParams: Map[String, String] = pathParams ++ query.toMap

  /** Current depth in the tree */
  def depth: Int = context.length

  /** Are we at the root? */
  def isRoot: Boolean = context.isEmpty

  /** Does the current node have a page to render? */
  def hasPage: Boolean = focus.page.isDefined

  /** Can we go up? */
  def canGoUp: Boolean = context.nonEmpty

  /** Can we go down via a specific edge? */
  def canGoDown(edge: String): Boolean = focus.child(edge).isDefined

  /** Available child edges */
  def availableEdges: Set[EdgeLabel] = focus.edgeLabels

  /** Breadcrumb trail from root to current position */
  def breadcrumbs: List[(String, Uri)] =
    val paths = context.reverse.scanLeft(Path.root) { (path, crumb) =>
      path / crumb.segment
    }
    paths.zip("Root" :: context.reverse.map(_.segment)).map {
      case (path, label) => (label, Uri(path, Query.empty, None))
    }

  // ===========================================================================
  // NAVIGATION
  // ===========================================================================

  /** Navigate down via a literal edge.
    *
    * @param segment
    *   The literal edge label (e.g., "users")
    * @return
    *   Some(newZipper) if edge exists, None otherwise
    */
  def goDown(segment: String): Option[RouteZipper[Page]] =
    focus.matchChild(segment).map { case (edge, childNode, captures) =>
      val sortedEdges = focus.children.keys.toList.sorted
      val idx = sortedEdges.indexOf(edge)
      val lefts = sortedEdges.take(idx)
      val rights = sortedEdges.drop(idx + 1)

      val crumb = RouteCrumb(
        edge = edge,
        capturedValue = captures.values.headOption.orElse(
          if edge.isInstanceOf[EdgeLabel.Literal] then None else Some(segment)
        ),
        parentNode = focus,
        leftSiblings = lefts,
        rightSiblings = rights
      )

      RouteZipper(childNode, crumb :: context, query, fragment)
    }

  /** Navigate down via a parameterized edge, providing the value.
    *
    * @param paramName
    *   The parameter name
    * @param value
    *   The value to capture
    */
  def goDownParam(paramName: String, value: String): Option[RouteZipper[Page]] =
    focus.children.collectFirst {
      case (edge @ EdgeLabel.Param(name, validator), childNode)
          if name == paramName && validator.forall(_.isValid(value)) =>
        val sortedEdges = focus.children.keys.toList.sorted
        val idx = sortedEdges.indexOf(edge)

        val crumb = RouteCrumb(
          edge = edge,
          capturedValue = Some(value),
          parentNode = focus,
          leftSiblings = sortedEdges.take(idx),
          rightSiblings = sortedEdges.drop(idx + 1)
        )

        RouteZipper(childNode, crumb :: context, query, fragment)
    }

  /** Navigate up to parent.
    */
  def goUp: Option[RouteZipper[Page]] = context match
    case Nil => None
    case crumb :: rest =>
      Some(RouteZipper(crumb.parentNode, rest, query, fragment))

  /** Navigate to left sibling.
    */
  def goLeft: Option[RouteZipper[Page]] = context match
    case Nil                                         => None
    case crumb :: rest if crumb.leftSiblings.isEmpty => None
    case crumb :: rest =>
      val leftEdge = crumb.leftSiblings.last
      crumb.parentNode.children.get(leftEdge).map { leftNode =>
        val newCrumb = RouteCrumb(
          edge = leftEdge,
          capturedValue =
            None, // Can't preserve captured value for different edge
          parentNode = crumb.parentNode,
          leftSiblings = crumb.leftSiblings.init,
          rightSiblings = crumb.edge :: crumb.rightSiblings
        )
        RouteZipper(leftNode, newCrumb :: rest, query, fragment)
      }

  /** Navigate to right sibling.
    */
  def goRight: Option[RouteZipper[Page]] = context match
    case Nil                                          => None
    case crumb :: rest if crumb.rightSiblings.isEmpty => None
    case crumb :: rest =>
      val rightEdge = crumb.rightSiblings.head
      crumb.parentNode.children.get(rightEdge).map { rightNode =>
        val newCrumb = RouteCrumb(
          edge = rightEdge,
          capturedValue = None,
          parentNode = crumb.parentNode,
          leftSiblings = crumb.leftSiblings :+ crumb.edge,
          rightSiblings = crumb.rightSiblings.tail
        )
        RouteZipper(rightNode, newCrumb :: rest, query, fragment)
      }

  /** Navigate to root.
    */
  def goToRoot: RouteZipper[Page] =
    goUp.map(_.goToRoot).getOrElse(this)

  /** Navigate following a path of segments.
    */
  def followPath(segments: List[String]): Option[RouteZipper[Page]] =
    segments.foldLeft(Option(this)) { (maybeZ, segment) =>
      maybeZ.flatMap(_.goDown(segment))
    }

  /** Navigate to a specific URI (from root).
    */
  def navigateTo(uri: Uri): Option[RouteZipper[Page]] =
    val fromRoot = goToRoot
    val segments = uri.path.segments.map(_.render)
    fromRoot.followPath(segments).map { z =>
      z.copy(query = uri.query, fragment = uri.fragment)
    }

  // ===========================================================================
  // QUERY & FRAGMENT MANIPULATION
  // ===========================================================================

  /** Set a query parameter */
  def setQuery(key: String, value: String): RouteZipper[Page] =
    copy(query = query.set(key, value))

  /** Remove a query parameter */
  def removeQuery(key: String): RouteZipper[Page] =
    copy(query = query.remove(key))

  /** Replace all query parameters */
  def withQuery(newQuery: Query): RouteZipper[Page] =
    copy(query = newQuery)

  /** Clear all query parameters */
  def clearQuery: RouteZipper[Page] =
    copy(query = Query.empty)

  /** Set the fragment */
  def setFragment(frag: String): RouteZipper[Page] =
    copy(fragment = Some(Fragment(frag)))

  /** Clear the fragment */
  def clearFragment: RouteZipper[Page] =
    copy(fragment = None)

  // ===========================================================================
  // EXTRACTION
  // ===========================================================================

  /** Get the current page (if any) */
  def currentPage: Option[Page] = focus.page

  /** Get a typed parameter or fail */
  def param[A](name: String)(using read: Read[A]): Either[String, A] =
    pathParams
      .get(name)
      .orElse(query.get(name))
      .toRight(s"Parameter not found: $name")
      .flatMap(read.read)

  /** Get the route metadata */
  def meta: RouteMeta = focus.meta

  /** Convert to a MatchedRoute */
  def toMatchedRoute: Option[MatchedRoute[Page]] =
    focus.page.map { page =>
      MatchedRoute(
        node = focus,
        page = page,
        pathParams = pathParams,
        query = query,
        path = currentPath,
        breadcrumbs = context.reverse.map(c => (c.edge, c.parentNode)),
        fragment = fragment
      )
    }

object RouteZipper:
  /** Create a zipper at the root of a route tree */
  def fromRoot[Page](root: RouteNode[Page]): RouteZipper[Page] =
    RouteZipper(root, Nil)

  /** Create a zipper and navigate to a URI */
  def fromUri[Page](
      root: RouteNode[Page],
      uri: Uri
  ): Option[RouteZipper[Page]] =
    fromRoot(root).navigateTo(uri)

  /** Parse a URI string and navigate to it */
  def fromUriString[Page](
      root: RouteNode[Page],
      uriString: String
  ): Either[ParseError, Option[RouteZipper[Page]]] =
    Uri.parse(uriString).map(uri => fromUri(root, uri))

// =============================================================================
// ROUTER - Stateless matching and URL generation
// =============================================================================

/** A Router provides bidirectional URL ↔ Route mapping.
  *
  * It wraps a route tree and provides convenient matching and generation.
  */
final class Router[Page](val root: RouteNode[Page]):

  /** Match a URI to a route */
  def matchUri(uri: Uri): RouteResult[Page] =
    RouteZipper.fromUri(root, uri) match
      case Some(zipper) if zipper.hasPage =>
        RouteResult.Matched(zipper.toMatchedRoute.get)
      case Some(zipper) =>
        // Reached a node without a page - partial match
        RouteResult.NotFound(uri, Some(zipper.focus))
      case None =>
        RouteResult.NotFound(uri, None)

  /** Match a URI string */
  def matchUriString(uriString: String): Either[ParseError, RouteResult[Page]] =
    Uri.parse(uriString).map(matchUri)

  /** Create a zipper at the root */
  def zipper: RouteZipper[Page] = RouteZipper.fromRoot(root)

  /** Generate a URI for a specific page (requires a lookup function) */
  def uriFor(page: Page, params: Map[String, String] = Map.empty): Option[Uri] =
    findPage(root, page, Path.root, params)

  private def findPage(
      node: RouteNode[Page],
      target: Page,
      currentPath: Path,
      params: Map[String, String]
  ): Option[Uri] =
    if node.page.contains(target) then Some(Uri(currentPath))
    else
      node.children.view.flatMap { case (edge, child) =>
        val segment = edge match
          case EdgeLabel.Literal(v)     => v
          case EdgeLabel.Param(name, _) => params.getOrElse(name, s":$name")
          case EdgeLabel.Wildcard       => "*"
        findPage(child, target, currentPath / segment, params)
      }.headOption

object Router:
  def apply[Page](root: RouteNode[Page]): Router[Page] = new Router(root)
