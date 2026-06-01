package tags.route

import tags.{Attr, Cursor, Depth, ElementKind, Node}

/** Route-marking extension.
  *
  * Import alongside `tags.dsl.*`:
  *
  * {{{
  * import tags.dsl.*
  * import tags.route.dsl.*
  * }}}
  */
object dsl:

  /** Mark a cursor's root element with a route path.
    *
    * Adds a `data-route` attribute to the root element, enabling route
    * extraction for client-side routing.
    *
    * Example:
    * {{{
    * (~(section | Ids.page.home) >>^ content.toHtml).asRoute("/")
    * }}}
    */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[D, K])
    def asRoute(path: String): Cursor[D, K] =
      cursor.focus match
        case elem: Node.Element =>
          val newAttrs = elem.attrs :+ Attr.Data("route", path)
          cursor.copy(focus = elem.copy(attrs = newAttrs))
        case _ => cursor
