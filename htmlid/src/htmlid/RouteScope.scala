package htmlid

import scala.compiletime.constValue
import scala.compiletime.ops.string.+

/** A mapping between a URL route path and an HTML element ID.
  *
  * Used to connect client-side routing with DOM element visibility toggling.
  *
  * @param routePath
  *   The URL path segment (e.g., "/", "/todos")
  * @param elementId
  *   The HTML element ID to show/hide (e.g., "page-home", "page-todos")
  */
case class RouteMapping(routePath: String, elementId: String) derives CanEqual

/** Base class for defining route-aware ID scopes.
  *
  * Combines `IdScope` (element ID prefix) with a URL route path, so both are
  * declared in a single place with compile-time type safety.
  *
  * ==Usage==
  *
  * {{{
  * import htmlid.{RouteScope, RouteMapping}
  *
  * object routes:
  *   object home  extends RouteScope["/", "page-home"]
  *   object todos extends RouteScope["/todos", "page-todos"]
  *   object help  extends RouteScope["/help", "page-help"]
  *
  *   val all: List[RouteMapping] = List(
  *     home.routeMapping, todos.routeMapping, help.routeMapping
  *   )
  * }}}
  *
  * @tparam R
  *   The singleton string literal type for the route path
  * @tparam P
  *   The singleton string literal type for the element ID prefix
  */
abstract class RouteScope[R <: String, P <: String] extends IdScope[P]:

  /** Extract the compile-time route path and element ID as a runtime
    * `RouteMapping`.
    */
  inline def routeMapping: RouteMapping =
    RouteMapping(constValue[R], constValue[P])

  /** The URL route path for this scope. */
  inline def routePath: String = constValue[R]

  /** Base class for nested sub-routes within this route scope.
    *
    * {{{
    * object todos extends RouteScope["/todos", "page-todos"]:
    *   object detail extends SubRoute["detail", "detail"]
    *   // routePath = "/todos/detail", element prefix = "page-todos-detail"
    * }}}
    *
    * @tparam RS
    *   The singleton string literal for the sub-route path segment
    * @tparam S
    *   The singleton string literal for the element ID sub-segment
    */
  abstract class SubRoute[RS <: String, S <: String]
      extends RouteScope[R + "/" + RS, P + "-" + S]
