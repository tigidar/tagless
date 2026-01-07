package zippermealey.routing

import scala.concurrent.duration.*

/**
 * =============================================================================
 * DECLARATIVE ROUTING DSL
 * =============================================================================
 * 
 * This DSL provides a clean, readable way to define routes that captures:
 *   1. The tree structure (hierarchy of routes)
 *   2. Edge labels (path segments)
 *   3. Parameter capture (with type safety)
 *   4. Query param handling
 *   5. Metadata (auth, caching, etc.)
 * 
 * =============================================================================
 * DESIGN GOALS
 * =============================================================================
 * 
 * The DSL should read like a description of your application structure:
 * 
 *   routes {
 *     "users" / {
 *       index -> UsersPage
 *       userId / {
 *         index -> UserDetailPage
 *         "posts" / {
 *           index -> UserPostsPage
 *           postId -> PostPage
 *         }
 *       }
 *     }
 *     "about" -> AboutPage
 *   }
 * 
 * This directly mirrors the URL structure:
 *   /users              → UsersPage
 *   /users/:userId      → UserDetailPage
 *   /users/:userId/posts → UserPostsPage
 *   /users/:userId/posts/:postId → PostPage
 *   /about              → AboutPage
 * 
 * =============================================================================
 * KEY CONCEPTS
 * =============================================================================
 * 
 * 1. PATH SEGMENTS: Literal strings or parameter captures
 *    "users"          - Literal segment
 *    userId           - Parameter capture (defined earlier)
 * 
 * 2. ROUTE TARGETS: What to render at a path
 *    -> UsersPage     - Render this page
 *    / { ... }        - Has children (may also have index page)
 * 
 * 3. INDEX PAGES: The page for a container route
 *    index -> UsersPage   - /users renders UsersPage
 *                         - /users/:id renders children
 * 
 * 4. PARAMETERS: Type-safe captures
 *    val userId = param[UserId]("userId")
 *    userId / { ... }     - :userId segment
 * 
 * 5. QUERY PARAMS: Filter/state in query string
 *    withQuery[Filters] { filters => ... }
 */

// =============================================================================
// PARAMETER DEFINITIONS
// =============================================================================

/**
 * A parameter definition for capturing URL segments.
 * 
 * This is created once and reused:
 *   val userId = param[UserId]("userId")
 * 
 * Then used in routes:
 *   userId / { ... }
 */
final case class ParamDef[A](
  name: String,
  validator: Option[ParamValidator] = None
)(using val read: Read[A], val show: Show[A]):
  
  /** Create the edge label for this param */
  def toEdgeLabel: EdgeLabel = EdgeLabel.Param(name, validator)
  
  /** Add validation */
  def validate(v: ParamValidator): ParamDef[A] = copy(validator = Some(v))
  
  /** Validate as numeric */
  def numeric: ParamDef[A] = validate(ParamValidator.numeric)
  
  /** Validate as UUID */
  def uuid: ParamDef[A] = validate(ParamValidator.uuid)

object ParamDef:
  /** Create a string parameter */
  def string(name: String): ParamDef[String] = ParamDef[String](name)
  
  /** Create an int parameter */
  def int(name: String): ParamDef[Int] = ParamDef[Int](name).numeric
  
  /** Create a long parameter */
  def long(name: String): ParamDef[Long] = ParamDef[Long](name).numeric
  
  /** Create a UUID parameter */
  def uuid(name: String): ParamDef[java.util.UUID] = ParamDef[java.util.UUID](name).uuid

// =============================================================================
// DSL BUILDING BLOCKS
// =============================================================================

/**
 * A route builder that accumulates route definitions.
 */
final class RouteBuilder[Page]:
  private var children: Map[EdgeLabel, RouteNode[Page]] = Map.empty
  private var indexPage: Option[Page] = None
  private var queryCodec: Option[QueryCodec[?]] = None
  private var meta: RouteMeta = RouteMeta.empty
  
  /** Set the index page for this route */
  def index: IndexBuilder[Page] = new IndexBuilder(this)
  
  /** Add a literal child route */
  def literal(segment: String): ChildBuilder[Page] = 
    new ChildBuilder(this, EdgeLabel.Literal(segment))
  
  /** Add a parameterized child route */
  def param[A](p: ParamDef[A]): ChildBuilder[Page] =
    new ChildBuilder(this, p.toEdgeLabel)
  
  /** Set route metadata */
  def withMeta(m: RouteMeta): RouteBuilder[Page] =
    meta = m
    this
  
  /** Set query codec */
  def withQueryCodec[Q](codec: QueryCodec[Q]): RouteBuilder[Page] =
    queryCodec = Some(codec)
    this
  
  // Internal setters
  private[routing] def setIndexPage(page: Page): Unit = indexPage = Some(page)
  private[routing] def addChild(label: EdgeLabel, node: RouteNode[Page]): Unit =
    children = children + (label -> node)
  
  /** Build the route node */
  def build: RouteNode[Page] = RouteNode(indexPage, children, queryCodec, meta)

/**
 * Builder for index page assignment.
 */
final class IndexBuilder[Page](parent: RouteBuilder[Page]):
  def ->(page: Page): RouteBuilder[Page] =
    parent.setIndexPage(page)
    parent

/**
 * Builder for child routes.
 */
final class ChildBuilder[Page](parent: RouteBuilder[Page], edge: EdgeLabel):
  /** This is a leaf route */
  def ->(page: Page): RouteBuilder[Page] =
    parent.addChild(edge, RouteNode.leaf(page))
    parent
  
  /** This route has children */
  def /(block: RouteBuilder[Page] ?=> Unit): RouteBuilder[Page] =
    val childBuilder = new RouteBuilder[Page]
    block(using childBuilder)
    parent.addChild(edge, childBuilder.build)
    parent

// =============================================================================
// CONVENIENT DSL SYNTAX
// =============================================================================

/**
 * Main entry point for the routing DSL.
 */
def routes[Page](block: RouteBuilder[Page] ?=> Unit): RouteNode[Page] =
  val builder = new RouteBuilder[Page]
  block(using builder)
  builder.build

/**
 * Extension methods for string literals in DSL.
 */
extension (segment: String)
  /** Create a child route with literal segment */
  def /[Page](block: RouteBuilder[Page] ?=> Unit)(using parent: RouteBuilder[Page]): Unit =
    val childBuilder = new RouteBuilder[Page]
    block(using childBuilder)
    parent.addChild(EdgeLabel.Literal(segment), childBuilder.build)
  
  /** Create a leaf route with literal segment */
  def ->[Page](page: Page)(using parent: RouteBuilder[Page]): Unit =
    parent.addChild(EdgeLabel.Literal(segment), RouteNode.leaf(page))

/**
 * Extension methods for param definitions in DSL.
 */
extension [A](paramDef: ParamDef[A])
  /** Create a child route with param segment */
  def /[Page](block: RouteBuilder[Page] ?=> Unit)(using parent: RouteBuilder[Page]): Unit =
    val childBuilder = new RouteBuilder[Page]
    block(using childBuilder)
    parent.addChild(paramDef.toEdgeLabel, childBuilder.build)
  
  /** Create a leaf route with param segment */
  def ->[Page](page: Page)(using parent: RouteBuilder[Page]): Unit =
    parent.addChild(paramDef.toEdgeLabel, RouteNode.leaf(page))

/**
 * Index page helper.
 */
def index[Page](using parent: RouteBuilder[Page]): IndexBuilder[Page] = 
  parent.index

// =============================================================================
// TYPE-SAFE PARAM HELPERS
// =============================================================================

/** Create a parameter definition */
def param[A](name: String)(using Read[A], Show[A]): ParamDef[A] = ParamDef[A](name)

/** Common parameter patterns */
object params:
  def id(name: String = "id"): ParamDef[Long] = ParamDef.long(name)
  def uuid(name: String = "id"): ParamDef[java.util.UUID] = ParamDef.uuid(name)
  def slug(name: String = "slug"): ParamDef[String] = ParamDef.string(name)

// =============================================================================
// ROUTE METADATA DSL
// =============================================================================

/** Set metadata in DSL */
def withMeta[Page](meta: RouteMeta)(using parent: RouteBuilder[Page]): Unit =
  parent.withMeta(meta)

/** Set title */
def title[Page](t: String)(using parent: RouteBuilder[Page]): Unit =
  parent.withMeta(RouteMeta(title = Some(t)))

/** Mark as requiring auth */
def requireAuth[Page](using parent: RouteBuilder[Page]): Unit =
  parent.withMeta(RouteMeta(requiresAuth = true))

// =============================================================================
// QUERY PARAM DSL
// =============================================================================

/**
 * Define a route with typed query parameters.
 */
def withQuery[Q, Page](codec: QueryCodec[Q])(block: RouteBuilder[Page] ?=> Unit)(using parent: RouteBuilder[Page]): Unit =
  parent.withQueryCodec(codec)
  block

// =============================================================================
// ALTERNATIVE FLUENT API
// =============================================================================

/**
 * For those who prefer a more fluent style:
 * 
 *   Route.root[Page]
 *     .child("users")
 *       .child(userId)
 *         .leaf(UserPage)
 *       .end
 *       .leaf(UsersPage)
 *     .end
 *     .build
 */
object Route:
  def root[Page]: FluentRouteBuilder[Page] = 
    new FluentRouteBuilder(RouteNode.empty[Page], Nil)

final class FluentRouteBuilder[Page](
  current: RouteNode[Page],
  stack: List[(EdgeLabel, RouteNode[Page])]
):
  /** Add a literal child segment */
  def child(segment: String): FluentRouteBuilder[Page] =
    new FluentRouteBuilder(RouteNode.empty[Page], (EdgeLabel.Literal(segment), current) :: stack)
  
  /** Add a param child segment */
  def child[A](paramDef: ParamDef[A]): FluentRouteBuilder[Page] =
    new FluentRouteBuilder(RouteNode.empty[Page], (paramDef.toEdgeLabel, current) :: stack)
  
  /** Set this as a leaf with a page */
  def leaf(page: Page): FluentRouteBuilder[Page] =
    val newNode = current.copy(page = Some(page))
    stack match
      case Nil => new FluentRouteBuilder(newNode, Nil)
      case (edge, parent) :: rest =>
        val updatedParent = parent.copy(children = parent.children + (edge -> newNode))
        new FluentRouteBuilder(updatedParent, rest)
  
  /** End current level, go back to parent */
  def end: FluentRouteBuilder[Page] =
    stack match
      case Nil => this
      case (edge, parent) :: rest =>
        val updatedParent = parent.copy(children = parent.children + (edge -> current))
        new FluentRouteBuilder(updatedParent, rest)
  
  /** Build the final route tree */
  def build: RouteNode[Page] =
    stack.foldLeft(current) { case (child, (edge, parent)) =>
      parent.copy(children = parent.children + (edge -> child))
    }
