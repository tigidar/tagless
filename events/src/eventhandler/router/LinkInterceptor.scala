package eventhandler.router

import com.raquo.airstream.ownership.Owner
import eventhandler.events.DomEventSource
import eventhandler.events.EventFilter.*
import htmlid.RouteMapping
import org.scalajs.dom

/** Intercepts clicks on navigation links and routes them through the `Router`.
  *
  * Given a link ID prefix (e.g., `"link-"`), intercepts clicks on elements
  * whose ID starts with that prefix and maps them to route paths via a provided
  * lookup map.
  *
  * ==Usage==
  * {{{
  * val linkToRoute = Map("link-home" -> "/", "link-todos" -> "/todos")
  * val interceptor = LinkInterceptor(router, "link-", linkToRoute)
  * }}}
  *
  * Or, derive link-to-route mapping from `RouteMapping` objects:
  * {{{
  * val interceptor = LinkInterceptor.fromMappings(router, Ids.routes.all)
  * }}}
  */
class LinkInterceptor(
    router: Router,
    prefix: String,
    linkToRoute: Map[String, String]
)(using owner: Owner):

  private val clicks = DomEventSource.clicks(dom.document)

  val _ = clicks.forPrefix(prefix).foreach { click =>
    click.preventDefault()
    click.targetId.flatMap(linkToRoute.get).foreach { routePath =>
      router.navigateTo(routePath)
    }
  }

object LinkInterceptor:
  /** Create a LinkInterceptor by deriving link IDs from element IDs.
    *
    * Uses naming convention: element ID "page-home" -> link ID "link-home"
    *
    * @param router
    *   The router instance
    * @param mappings
    *   Route mappings from RouteScope declarations
    * @param linkPrefix
    *   Prefix for link IDs (default: "link-")
    * @param elementPrefix
    *   Prefix to strip from element IDs (default: "page-")
    */
  def fromMappings(
      router: Router,
      mappings: Seq[RouteMapping],
      linkPrefix: String = "link-",
      elementPrefix: String = "page-"
  )(using Owner): LinkInterceptor =
    val linkToRoute = mappings.map { m =>
      val name = m.elementId.stripPrefix(elementPrefix)
      val linkId = linkPrefix + name
      linkId -> m.routePath
    }.toMap
    new LinkInterceptor(router, linkPrefix, linkToRoute)
