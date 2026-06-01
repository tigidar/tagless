package tags

/** Fragment containing multiple sibling nodes.
  *
  * Used for operations like multiplication (li * 3) or combining multiple
  * cursors with the + operator.
  *
  * Also provides builders for creating fragments from collections:
  *   - Fragment.from(items)(f) - map collection to fragment
  *   - Fragment.indexed(n)(f) - create n items with index
  */
final case class Fragment(nodes: Vector[Node])

object Fragment:
  val empty: Fragment = Fragment(Vector.empty)

  def apply(nodes: Node*): Fragment =
    Fragment(nodes.toVector)

  // ============================================================
  // Collection Builders
  // ============================================================

  /** Build a fragment by mapping over a collection.
    *
    * Example:
    * {{{
    * Fragment.from(List("Alice", "Bob")) { name =>
    *   li(name)
    * }
    * }}}
    */
  def from[A](items: Iterable[A])(f: A => FragmentContent): Fragment =
    Fragment(items.iterator.map(a => toNode(f(a))).toVector)

  /** Build a fragment with indexed iteration (1 to n).
    *
    * Example:
    * {{{
    * Fragment.indexed(3) { i =>
    *   li(s"Item $i")  // Item 1, Item 2, Item 3
    * }
    * }}}
    */
  def indexed(n: Int)(f: Int => FragmentContent): Fragment =
    Fragment((1 to n).iterator.map(i => toNode(f(i))).toVector)

  // ============================================================
  // Helper types and conversions
  // ============================================================

  /** Union type for content that can be converted to a Node */
  type FragmentContent = Tag[?] | Cursor[?, ?] | Node

  /** Convert FragmentContent to Node */
  private def toNode(content: FragmentContent): Node =
    content match
      case t: Tag[?]       => tagToNode(t)
      case c: Cursor[?, ?] => c.seal
      case n: Node         => n

  /** Convert a Tag to Node (mirrors dsl.tagToNode logic) */
  private def tagToNode[K <: ElementKind](tag: Tag[K]): Node =
    if isVoidElement(tag.name)
    then Node.voidElement(tag.name, tag.attrs)
    else Node.element(tag.name, tag.attrs)

  /** Check if element name is a void element */
  private def isVoidElement(name: String): Boolean =
    name == "img" || name == "br" || name == "hr" ||
      name == "input" || name == "meta" || name == "link" ||
      name == "base" || name == "col" || name == "embed" ||
      name == "source" || name == "track" || name == "wbr" ||
      name == "area" ||
      // SVG void elements
      name == "circle" || name == "rect" || name == "ellipse" ||
      name == "line" || name == "polyline" || name == "polygon" ||
      name == "path" || name == "use" || name == "stop" ||
      name == "image" || name == "animate" || name == "animateTransform" ||
      name == "set" || name == "mpath" ||
      // SVG filter void elements
      name == "feGaussianBlur" || name == "feColorMatrix" ||
      name == "feBlend" || name == "feOffset" || name == "feMergeNode"

  // ============================================================
  // HTML Pattern Helpers
  // ============================================================

  /** CSS stylesheet link elements.
    *
    * Example:
    * {{{
    * head.toCursor >> Fragment.stylesheets("/css/app.css", "/css/vendor.css")
    * Fragment.stylesheets("/css/")("app", "vendor")  // adds .css suffix
    * }}}
    */
  def stylesheets(paths: String*): Fragment =
    Fragment(paths.iterator.map { path =>
      Node.voidElement(
        "link",
        List(
          Attr.Custom("rel", "stylesheet"),
          Attr.Href(path)
        )
      )
    }.toVector)

  /** CSS stylesheets with prefix (adds .css suffix).
    *
    * Example:
    * {{{
    * Fragment.stylesheets("/css/")("flatten", "layout", "styles")
    * // Creates links to /css/flatten.css, /css/layout.css, /css/styles.css
    * }}}
    */
  def stylesheets(prefix: String)(names: String*): Fragment =
    stylesheets(names.map(n => s"$prefix$n.css")*)

  /** Select option elements with value/text pairs.
    *
    * Example:
    * {{{
    * select.toCursor >> Fragment.options(("light", texts.light), ("dark", texts.dark))
    * }}}
    */
  def options(pairs: (String, i18n.I18n[?])*)(using lang: i18n.Lang): Fragment =
    Fragment(pairs.iterator.map { case (value, text) =>
      Node.Element(
        "option",
        List(Attr.Value(value)),
        Vector(Node.Text(text.apply))
      )
    }.toVector)

  /** Select option elements where value equals display text.
    *
    * Example:
    * {{{
    * select.toCursor >> Fragment.simpleOptions(texts.system, texts.light, texts.dark)
    * }}}
    */
  def simpleOptions(texts: i18n.I18n[?]*)(using lang: i18n.Lang): Fragment =
    Fragment(texts.iterator.map { text =>
      Node.Element("option", Nil, Vector(Node.Text(text.apply)))
    }.toVector)

  /** Navigation menu items: li > a with href and translated text.
    *
    * Example:
    * {{{
    * given Lang = lang.en
    * ul.toCursor >> Fragment.navItems(
    *   "#home"  -> texts.nav.home,
    *   "#about" -> texts.nav.about
    * )
    * }}}
    */
  def navItems(
      items: (String, i18n.I18n[?])*
  )(using lang: i18n.Lang): Fragment =
    Fragment(items.iterator.map { case (href, text) =>
      Node.Element(
        "li",
        Nil,
        Vector(
          Node.Element(
            "a",
            List(Attr.Href(href)),
            Vector(Node.Text(text.apply))
          )
        )
      )
    }.toVector)

  /** Simple list items with text content.
    *
    * Example:
    * {{{
    * ul.toCursor >> Fragment.listItems(texts.first, texts.second, texts.third)
    * }}}
    */
  def listItems(texts: i18n.I18n[?]*)(using lang: i18n.Lang): Fragment =
    Fragment(texts.iterator.map { text =>
      Node.Element("li", Nil, Vector(Node.Text(text.apply)))
    }.toVector)

  /** Hidden content section: div with id and "is-hidden" class.
    *
    * Example:
    * {{{
    * Fragment.hiddenSection("welcome-content").toCursor >>^ h1("Welcome!")
    * }}}
    */
  def hiddenSection(id: String): Tag[Normal] =
    Tag.normal("div") | Attr.Id(id) | Attr.Class("hidden")
