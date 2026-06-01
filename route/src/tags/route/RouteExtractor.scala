package tags.route

import tags.{Node, Attr}

/** A route entry mapping a URL path to an HTML element ID.
  *
  * Extracted from `data-route` and `id` attributes in the Node tree.
  */
case class RouteEntry(routePath: String, elementId: String) derives CanEqual

/** Extracts route entries from a Node tree.
  *
  * Walks the tree looking for elements that have both a `data-route` attribute
  * and an `id` attribute, collecting them as route-to-element mappings.
  *
  * ==Usage==
  * {{{
  * import tags.route.RouteExtractor
  *
  * val node = page.seal
  * val routes = RouteExtractor.extract(node)
  * // List(RouteEntry("/", "page-home"), RouteEntry("/todos", "page-todos"), ...)
  * }}}
  */
object RouteExtractor:

  /** Extract all route entries from a Node tree. */
  def extract(node: Node): List[RouteEntry] =
    val buf = List.newBuilder[RouteEntry]
    walk(node, buf)
    buf.result()

  private def walk(
      node: Node,
      buf: collection.mutable.Builder[RouteEntry, List[RouteEntry]]
  ): Unit =
    node match
      case Node.Element(_, attrs, children) =>
        val routePath = attrs.collectFirst { case Attr.Data("route", v) => v }
        val elemId = attrs.collectFirst { case Attr.Id(v) => v }
        (routePath, elemId) match
          case (Some(route), Some(id)) =>
            buf += RouteEntry(route, id)
          case _ => ()
        children.foreach(walk(_, buf))

      case Node.Fragment(nodes) =>
        nodes.foreach(walk(_, buf))

      case _: Node.VoidElement | _: Node.Text => ()
