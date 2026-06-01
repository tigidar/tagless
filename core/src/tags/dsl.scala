package tags

import scala.annotation.targetName
import tags.i18n.{I18n, Lang}

/** DSL operators for building HTML trees.
  *
  * Operators:
  *   - ~ : Prefix operator to create cursor from tag (alias for toCursor)
  *   - >> : Add child and descend into it
  *   - >>^: Add child and stay at current level
  *   - > : Add sibling and move focus to it
  *   - >^ : Add sibling and stay at current focus
  *   - ^ : Ascend one level
  *   - ^^ : Ascend two levels
  *   - * : Multiply element (create n copies)
  *   - + : Combine into fragment
  */
object dsl:

  /** Helper to create a Node from a Tag */
  private def tagToNode[K <: ElementKind](tag: Tag[K]): Node =
    // Use runtime check since we can't pattern match on phantom types
    if tag.name == "img" || tag.name == "br" || tag.name == "hr" ||
      tag.name == "input" || tag.name == "meta" || tag.name == "link" ||
      tag.name == "base" || tag.name == "col" || tag.name == "embed" ||
      tag.name == "source" || tag.name == "track" || tag.name == "wbr" ||
      tag.name == "area" ||
      // SVG void elements
      tag.name == "circle" || tag.name == "rect" || tag.name == "ellipse" ||
      tag.name == "line" || tag.name == "polyline" || tag.name == "polygon" ||
      tag.name == "path" || tag.name == "use" || tag.name == "stop" ||
      tag.name == "image" || tag.name == "animate" || tag.name == "animateTransform" ||
      tag.name == "set" || tag.name == "mpath" ||
      // SVG filter void elements
      tag.name == "feGaussianBlur" || tag.name == "feColorMatrix" ||
      tag.name == "feBlend" || tag.name == "feOffset" || tag.name == "feMergeNode"
    then Node.voidElement(tag.name, tag.attrs)
    else Node.element(tag.name, tag.attrs)

  // ============================================================
  // Tag to Cursor conversion
  // ============================================================

  /** Start building from a tag (works for both Normal and Void) */
  extension [K <: ElementKind](tag: Tag[K])
    def toCursor: Cursor[D0, K] =
      val node = tagToNode(tag)
      Cursor[D0, K](node, Nil)

    /** Concise prefix alias for `toCursor`: `~div >> p` is equivalent to
      * `div.toCursor >> p`.
      */
    def unary_~ : Cursor[D0, K] = toCursor

  // ============================================================
  // Child operators (only available on Normal elements)
  // ============================================================

  /** Add child and descend - for Normal parent */
  extension [D <: Depth](cursor: Cursor[D, Normal])

    /** Add a tag as child and descend into it */
    def >>[K2 <: ElementKind](child: Tag[K2]): Cursor[Succ[D], K2] =
      val childNode = tagToNode(child)
      Cursor.addChildAndDescend[D, K2](cursor, childNode)

    /** Add a sub-cursor as child and descend */
    @targetName("descendCursor")
    def >>[D2 <: Depth, K2 <: ElementKind](
        child: Cursor[D2, K2]
    ): Cursor[Succ[D], K2] =
      Cursor.addChildAndDescend[D, K2](cursor, child.seal)

    /** Add a fragment as children and stay */
    @targetName("descendFragment")
    def >>(fragment: Fragment): Cursor[D, Normal] =
      fragment.nodes.foldLeft(cursor) { (c, node) =>
        Cursor.addChildAndStay(c, node)
      }

    /** Add a tag as child and stay at current level */
    def >>^[K2 <: ElementKind](child: Tag[K2]): Cursor[D, Normal] =
      val childNode = tagToNode(child)
      Cursor.addChildAndStay(cursor, childNode)

    /** Add a sub-cursor as child and stay */
    @targetName("stayCursor")
    def >>^[D2 <: Depth, K2 <: ElementKind](
        child: Cursor[D2, K2]
    ): Cursor[D, Normal] =
      Cursor.addChildAndStay(cursor, child.seal)

    /** Add a fragment as children and stay */
    @targetName("stayFragment")
    def >>^(fragment: Fragment): Cursor[D, Normal] =
      fragment.nodes.foldLeft(cursor) { (c, node) =>
        Cursor.addChildAndStay(c, node)
      }

    /** Add a node directly as child and stay.
      *
      * Useful for inserting pre-built nodes, such as those converted from other
      * DSLs (e.g., Table DSL).
      */
    @targetName("stayNode")
    def >>^(node: Node): Cursor[D, Normal] =
      Cursor.addChildAndStay(cursor, node)

  // ============================================================
  // Sibling operators (only available when not at root)
  // ============================================================

  /** Add sibling operators - only when depth > 0 */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[Succ[D], K])

    /** Add a tag as sibling and move focus to it */
    def >[K2 <: ElementKind](sibling: Tag[K2]): Cursor[Succ[D], K2] =
      val siblingNode = tagToNode(sibling)
      Cursor.addSiblingAndMove[D, K, K2](cursor, siblingNode)

    /** Add a tag as sibling and stay at current focus */
    def >^[K2 <: ElementKind](sibling: Tag[K2]): Cursor[Succ[D], K] =
      val siblingNode = tagToNode(sibling)
      Cursor.addSiblingAndStay(cursor, siblingNode)

  // ============================================================
  // Ascent operators
  // ============================================================

  /** Ascend one level - only available when depth > 0 */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[Succ[D], K])
    def ^ : Cursor[D, Normal] =
      Cursor.ascend(cursor)

  /** Ascend two levels - only available when depth > 1 */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[Succ[Succ[D]], K])
    def ^^ : Cursor[D, Normal] =
      Cursor.ascend(Cursor.ascend(cursor))

  // ============================================================
  // Fragment operations
  // ============================================================

  /** Multiply a tag to create a fragment */
  extension [K <: ElementKind](tag: Tag[K])
    def *(n: Int): Fragment =
      val node = tagToNode(tag)
      Fragment(Vector.fill(n)(node))

  /** Collection to fragment extension */
  extension [A](items: Iterable[A])
    /** Map collection to fragment.
      *
      * Example:
      * {{{
      * List("Alice", "Bob").toFragment(name => li(name))
      * }}}
      */
    def toFragment(f: A => Fragment.FragmentContent): Fragment =
      Fragment.from(items)(f)

  /** Combine cursors into a fragment */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[D, K])
    def +[K2 <: ElementKind](other: Cursor[D, K2]): Fragment =
      Fragment(Vector(cursor.seal, other.seal))

  extension (fragment: Fragment)
    @targetName("plusCursor")
    def +[D <: Depth, K <: ElementKind](cursor: Cursor[D, K]): Fragment =
      Fragment(fragment.nodes :+ cursor.seal)

    @targetName("plusFragment")
    def +(other: Fragment): Fragment =
      Fragment(fragment.nodes ++ other.nodes)

  // ============================================================
  // Rendering
  // ============================================================

  /** Render cursor to HTML string */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[D, K])
    def toHtml: String =
      render.Html.render(cursor.seal)

    /** Render cursor to indented HTML string for debugging */
    def toPrettyHtml: String =
      render.PrettyHtml.render(cursor.seal)

    /** Render cursor to indented HTML string with custom indent */
    def toPrettyHtml(indent: String): String =
      render.PrettyHtml.render(cursor.seal, indent)

  /** Render tag directly to HTML string */
  extension [K <: ElementKind](tag: Tag[K])
    @targetName("tagToHtml")
    def toHtml: String =
      render.Html.render(tagToNode(tag))

    /** Render tag to indented HTML string for debugging */
    @targetName("tagToPrettyHtml")
    def toPrettyHtml: String =
      render.PrettyHtml.render(tagToNode(tag))

    /** Render tag to indented HTML string with custom indent */
    @targetName("tagToPrettyHtmlIndent")
    def toPrettyHtml(indent: String): String =
      render.PrettyHtml.render(tagToNode(tag), indent)

  // ============================================================
  // I18n text content — translatable text with implicit Lang
  // ============================================================

  /** Add translated text as child and descend */
  extension [D <: Depth](cursor: Cursor[D, Normal])

    @targetName("descendI18n")
    def >>[K <: String](text: I18n[K])(using
        lang: Lang
    ): Cursor[Succ[D], Normal] =
      val textNode = Node.text(text.resolve(lang))
      Cursor.addChildAndDescend[D, Normal](cursor, textNode)

    @targetName("stayI18n")
    def >>^[K <: String](text: I18n[K])(using lang: Lang): Cursor[D, Normal] =
      Cursor.addChildAndStay(cursor, Node.text(text.resolve(lang)))

  /** Create a tag with translated text content and optional attributes */
  extension (tag: Tag[Normal])
    @targetName("applyI18n")
    def apply[K <: String](text: I18n[K], attrs: Attr*)(using
        lang: Lang
    ): Cursor[D0, Normal] =
      val mergedAttrs = tag.attrs ++ attrs
      val element = Node.Element(
        tag.name,
        mergedAttrs,
        Vector(Node.text(text.resolve(lang)))
      )
      Cursor[D0, Normal](element, Nil)

  // asComponent moved to tags.viz (data-component attrs are viz-only)
  // asRoute     moved to tags.route (data-route attrs are route-only)
  // visualize/toD3Json/toAsciiTree/toMermaid moved to tags.viz
