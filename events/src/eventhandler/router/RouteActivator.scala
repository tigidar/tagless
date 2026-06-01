package eventhandler.router

import com.raquo.airstream.ownership.Owner
import org.scalajs.dom
import htmlid.RouteMapping

/** Activates routes by toggling CSS `.hidden` class on page section elements.
  *
  * Given a `Router` and a mapping from route paths to element IDs,
  * `RouteActivator` subscribes to path changes and shows the matching element
  * while hiding all others.
  *
  * ==Usage==
  * {{{
  * val router = Router.create
  * val activator = RouteActivator(router, Map("/" -> "page-home", "/todos" -> "page-todos"))
  * activator.showRoute(dom.window.location.pathname)
  * }}}
  */
class RouteActivator(router: Router, routeMap: Map[String, String])(using
    owner: Owner
):

  private val allElementIds: Iterable[String] = routeMap.values

  // Subscribe to router path changes
  val _ = router.currentPath.foreach { path =>
    showRoute(path)
  }

  /** Show the element matching the given route path, hiding all others.
    *
    * If the path doesn't match any route, all elements are hidden.
    */
  def showRoute(path: String): Unit =
    allElementIds.foreach { elemId =>
      Option(dom.document.getElementById(elemId))
        .foreach(_.classList.add("hidden"))
    }
    routeMap.get(path).foreach { elemId =>
      Option(dom.document.getElementById(elemId))
        .foreach(_.classList.remove("hidden"))
    }

object RouteActivator:

  /** Create a RouteActivator from a sequence of `RouteMapping` values.
    *
    * {{{
    * val activator = RouteActivator.fromMappings(router, Ids.routes.all)
    * }}}
    */
  def fromMappings(router: Router, mappings: Seq[RouteMapping])(using
      Owner
  ): RouteActivator =
    val routeMap = mappings.map(m => m.routePath -> m.elementId).toMap
    new RouteActivator(router, routeMap)
