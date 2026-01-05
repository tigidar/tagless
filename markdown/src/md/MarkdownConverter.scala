package md

import dom.{Cursor, Node, NodeType, NormalType}
import Cursor.{*, given}
import tags.T
import tags.T.*
import Markdown.*
import tags.gen.HTMLHeadingElement

object MarkdownConverterExtensions {
  import dom.CursorExtensions.{*, given}

  given Conversion[String, Iterable[String]] with
    def apply(value: String): Iterable[String] = List(value)

}

object MarkdownConverter:
  import dom.CursorExtensions.{*, given}
  import MarkdownConverterExtensions.{*, given}

  extension [N1 <: NodeType, E1 <: tags.gen.Element](childCur: Cursor[N1, E1])
    def upAsParent[Np <: NodeType, Ep <: tags.gen.Element]: Cursor[Np, Ep] =
      // implementation can call your regular `up` and (internally) ascribe the parent types.
      // The key is: callers never see a cast; it’s encapsulated here.
      childCur.up.asInstanceOf[Cursor[Np, Ep]]

  def markdownToTag[N <: NodeType, E <: tags.gen.Element](
      m: H | BulletType | Paragraph,
      c: Cursor[N, E]
  )(using ev: N =:= NormalType): Cursor[N, E] = m match

    case H(value, level) =>
      level match
        case 1 =>
          c.addChildStay(h1(value))
        case 2 =>
          c.addChildStay(h2(value))
        case 3 =>
          c.addChildStay(h3(value))
        case 4 =>
          c.addChildStay(h4(value))
        case 5 =>
          c.addChildStay(h5(value))
        case 6 =>
          c.addChildStay(h6(value))

    case BulletType.Star(v) => c.addChildStay(li(v, cls := "bullet-star"))
    case BulletType.Dash(v) => c.addChildStay(li(v, cls := "bullet-dash"))
    case BulletType.Numbered(v, _) =>
      c.addChildStay(li(v, cls := "bullet-numbered"))

    case Paragraph(s) =>
      c.addChildStay(p(s))

  def withChild[
      N <: NodeType,
      E <: tags.gen.Element,
      N1 <: NodeType,
      E1 <: tags.gen.Element
  ](
      c: Cursor[N, E],
      child: Node[N1, E1]
  )(
      f: Cursor[N1, E1] => Cursor[N1, E1]
  )(using ev: N =:= NormalType): Cursor[N, E] = {
    val down: Cursor[N1, E1] =
      c.addChild(child) // focus at child (type changes)
    val edited: Cursor[N1, E1] = f(down) // do work at child
    edited.upAsParent[N, E] // go back, restoring parent type
  }

  def build[N <: NodeType, E <: tags.gen.Element](
      md: Markdown,
      c0: Cursor[N, E]
  )(using N =:= NormalType): Cursor[N, E] =
    TagType.chunked(md).foldLeft(c0) { (c, chunk) =>
      chunk.head match
        case TagType.Levelled(_) =>
          // open <ul> (or decide <ol> inside), add all li’s, then return to parent
          withChild(c, ul) { cu =>
            chunk.foldLeft(cu) { (cu, tag) =>
              tag match
                case TagType.Levelled(BulletType.Star(v)) =>
                  cu.addChildStay(li(v))
                case TagType.Levelled(BulletType.Dash(v)) =>
                  cu.addChildStay(li(v, cls := "dash"))
                case TagType.Levelled(BulletType.Numbered(v, _)) =>
                  cu.addChildStay(
                    li(v)
                  ) // numbering handled by <ol> if you choose it
                case _ => cu
            }
          }

        case TagType.Single(h: H) =>
          val value = h.value
          h.level match
            case 1 =>
              c.addChildStay(h1(value))
            case 2 =>
              c.addChildStay(h2(value))
            case 3 =>
              c.addChildStay(h3(value))
            case 4 =>
              c.addChildStay(h4(value))
            case 5 =>
              c.addChildStay(h5(value))
            case 6 =>
              c.addChildStay(h6(value))

        case TagType.Single(Paragraph(text)) =>
          c.addChildStay(p(text))
    }
