package md

import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.{Cursor, Node, D0, Succ, Depth, Normal}
import tags.i18n.{I18n, Lang}
import Markdown.*

object MarkdownConverter:

  def build(md: Markdown)(using lang: Lang): Cursor[D0, Normal] =
    val root = ~div
    TagType.chunked(md).foldLeft(root) { (cursor, chunk) =>
      chunk.head match
        case TagType.Levelled(_, _) =>
          // Build ul with li children, then ascend back
          val withUl = cursor >> ul
          val withItems = chunk.foldLeft(withUl) { (c, tag) =>
            tag match
              case TagType.Levelled(BulletType.Star(v, _), _) =>
                c >>^ li(v)
              case TagType.Levelled(BulletType.Dash(v, _), _) =>
                c >>^ (li | "dash".cls)(v)
              case TagType.Levelled(BulletType.Numbered(v, _, _), _) =>
                c >>^ li(v)
              case _ => c
          }
          withItems.^

        case TagType.Single(h: H) =>
          val value = h.value
          h.level match
            case 1 => cursor >>^ h1(value)
            case 2 => cursor >>^ h2(value)
            case 3 => cursor >>^ h3(value)
            case 4 => cursor >>^ h4(value)
            case 5 => cursor >>^ h5(value)
            case 6 => cursor >>^ h6(value)
            case _ => cursor >>^ h6(value)

        case TagType.Single(Paragraph(text)) =>
          cursor >>^ p(text)

        case TagType.Block(Blockquote(content)) =>
          // Recursively build the blockquote content and wrap in <blockquote>
          val innerNode = build(content).seal
          val blockquoteEl = Node.Element("blockquote", Nil, Vector(innerNode))
          cursor >>^ blockquoteEl

        case TagType.Code(CodeBlock(codeText, langOpt)) =>
          // Build <pre><code class="language-xxx">...</code></pre>
          val codeAttrs = langOpt
            .map(l => List(tags.Attr.Class(s"language-$l")))
            .getOrElse(Nil)
          val codeEl =
            Node.Element("code", codeAttrs, Vector(Node.Text(codeText)))
          val preEl = Node.Element("pre", Nil, Vector(codeEl))
          cursor >>^ preEl

        case TagType.Rich(RichParagraph(content)) =>
          // Render inline content and wrap in <p>
          val innerNode = InlineMarkdown.render(content)
          val pEl = Node.Element("p", Nil, Vector(innerNode))
          cursor >>^ pEl
    }
