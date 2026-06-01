package md

import tags.{Node, Attr}
import tags.i18n.{I18n, Lang}

/** ADT for inline text formatting (bold, italic, code, links, images) */
enum InlineMarkdown derives CanEqual:
  case Text(content: String)
  case Bold(content: InlineMarkdown)
  case Italic(content: InlineMarkdown)
  case Underline(content: InlineMarkdown)
  case Code(content: String)
  case Math(expression: String)
  case MathBlock(expression: String)
  case Link(text: InlineMarkdown, href: String)
  case Image(src: String, alt: String)
  case Sequence(items: Vector[InlineMarkdown])
  case Bilingual(langs: Vector[(String, InlineMarkdown)])

object InlineMarkdown:
  def text(s: String): InlineMarkdown = Text(s)

  extension (s: String)
    def bold: InlineMarkdown = Bold(Text(s))
    def italic: InlineMarkdown = Italic(Text(s))
    def underline: InlineMarkdown = Underline(Text(s))
    def code: InlineMarkdown = Code(s)
    def math: InlineMarkdown = Math(s)
    def mathBlock: InlineMarkdown = MathBlock(s)
    def link(href: String): InlineMarkdown = Link(Text(s), href)
    def img(alt: String): InlineMarkdown = Image(src = s, alt = alt)

  extension (sc: StringContext)
    def md(args: InlineMarkdown*): InlineMarkdown =
      val parts = sc.parts.iterator
      val splices = args.iterator
      val items = Vector.newBuilder[InlineMarkdown]

      def addParsed(s: String): Unit =
        if s.nonEmpty then
          InlineMarkdownParser.parse(s) match
            case Sequence(parsedItems) => items ++= parsedItems
            case single                => items += single

      addParsed(parts.next())
      while splices.hasNext do
        items += splices.next()
        addParsed(parts.next())

      items.result() match
        case Vector()    => Text("")
        case Vector(one) => one
        case many        => Sequence(many)

  extension (inline: InlineMarkdown)
    def bold: InlineMarkdown = Bold(inline)
    def italic: InlineMarkdown = Italic(inline)
    def underline: InlineMarkdown = Underline(inline)
    def link(href: String): InlineMarkdown = Link(inline, href)

    def +(other: InlineMarkdown): InlineMarkdown = (inline, other) match
      case (Sequence(a), Sequence(b)) => Sequence(a ++ b)
      case (Sequence(a), b)           => Sequence(a :+ b)
      case (a, Sequence(b))           => Sequence(a +: b)
      case (a, b)                     => Sequence(Vector(a, b))

  /** Extension methods on I18n to create InlineMarkdown with formatting */
  extension [K <: String](i18n: I18n[K])
    def toInline(using Lang): InlineMarkdown = InlineMarkdown.Text(i18n.apply)
    def inlineBold(using Lang): InlineMarkdown = InlineMarkdown.Bold(toInline)
    def inlineItalic(using Lang): InlineMarkdown =
      InlineMarkdown.Italic(toInline)
    def inlineUnderline(using Lang): InlineMarkdown =
      InlineMarkdown.Underline(toInline)
    def inlineCode(using Lang): InlineMarkdown = InlineMarkdown.Code(i18n.apply)
    def inlineMath(using Lang): InlineMarkdown = InlineMarkdown.Math(i18n.apply)
    def inlineLink(href: String)(using Lang): InlineMarkdown =
      InlineMarkdown.Link(toInline, href)

    /** Render all translations as bilingual spans for runtime toggling.
      *
      * Produces `<span lang="en">English</span><span lang="nb">Norwegian</span>`
      * etc. CSS rules on a parent `[data-lang]` attribute toggle visibility.
      */
    def bi: InlineMarkdown =
      InlineMarkdown.Bilingual(
        i18n.translations.toVector.map((lang, text) =>
          (lang.code, InlineMarkdown.Text(text))
        )
      )

    /** Bilingual with markdown parsing (supports **bold**, *italic*, etc.) */
    def biMd: InlineMarkdown =
      InlineMarkdown.Bilingual(
        i18n.translations.toVector.map((lang, text) =>
          (lang.code, InlineMarkdownParser.parse(text))
        )
      )

  /** Render InlineMarkdown to a Node */
  def render(inline: InlineMarkdown): Node = inline match
    case InlineMarkdown.Text(s) => Node.Text(s)
    case InlineMarkdown.Bold(c) =>
      Node.Element("strong", Nil, Vector(render(c)))
    case InlineMarkdown.Italic(c) =>
      Node.Element("em", Nil, Vector(render(c)))
    case InlineMarkdown.Underline(c) =>
      Node.Element("u", Nil, Vector(render(c)))
    case InlineMarkdown.Code(s) =>
      Node.Element("code", Nil, Vector(Node.Text(s)))
    case InlineMarkdown.Math(s) =>
      Node.Element(
        "span",
        List(Attr.Class("katex-inline")),
        Vector(Node.Text(s))
      )
    case InlineMarkdown.MathBlock(s) =>
      Node.Element(
        "div",
        List(Attr.Class("katex-display")),
        Vector(Node.Text(s))
      )
    case InlineMarkdown.Link(text, href) =>
      Node.Element("a", List(Attr.Href(href)), Vector(render(text)))
    case InlineMarkdown.Image(src, alt) =>
      Node.VoidElement("img", List(Attr.Src(src), Attr.Alt(alt)))
    case InlineMarkdown.Sequence(items) =>
      Node.Fragment(items.map(render))
    case InlineMarkdown.Bilingual(langs) =>
      Node.Fragment(langs.map { (code, content) =>
        Node.Element("span", List(Attr.Custom("lang", code)), Vector(render(content)))
      })
