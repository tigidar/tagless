package md

import Markdown.*

enum TagType:
  case Single(md: H | Paragraph)
  case Levelled(md: BulletType)

object TagType:
  /** Flatten Markdown AST into a linear stream of TagType */
  def linear(md: Markdown): IndexedSeq[TagType] = md match
    case Append(l, r) => linear(l) ++ linear(r)
    case Bullet(bt)   => IndexedSeq(Levelled(bt))
    case h: H         => IndexedSeq(Single(h))
    case p: Paragraph => IndexedSeq(Single(p))
    case Init         => IndexedSeq.empty

  /** Group consecutive Levelled items into chunks; Singles become singleton chunks */
  def chunked(md: Markdown): List[IndexedSeq[TagType]] =
    chunkRuns(linear(md))

  /** Allocation-light chunker that groups consecutive Levelled items */
  private def chunkRuns(xs: IndexedSeq[TagType]): List[IndexedSeq[TagType]] =
    import scala.collection.mutable.ListBuffer
    val out = ListBuffer.empty[IndexedSeq[TagType]]
    var i = 0
    val n = xs.length

    while i < n do
      xs(i) match
        case _: Levelled =>
          val start = i
          i += 1
          while i < n && xs(i).isInstanceOf[Levelled] do i += 1
          out += xs.slice(start, i)
        case s: Single =>
          out += IndexedSeq(s)
          i += 1
    out.toList
