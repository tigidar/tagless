package md

import Markdown.*

enum TagType:
  case Single(md: H | Paragraph)
  case Levelled(md: BulletType)

object TagType:
  // 1) Flatten Markdown -> linear stream of TagType
  def linear(md: Markdown): IndexedSeq[TagType] =
    md match
      case Append(l, r) => linear(l) ++ linear(r)
      case Bullet(bt)   => IndexedSeq(TagType.Levelled(bt))
      case h: H         => IndexedSeq(TagType.Single(h))
      case p: Paragraph => IndexedSeq(TagType.Single(p))
      case Init         => IndexedSeq.empty

  // 2) Group consecutive Levelled(...) into one chunk; Singles become singleton chunks
  def chunked(md: Markdown): List[IndexedSeq[TagType]] =
    chunkRuns(linear(md))

  // Pure, allocation-light chunker over an IndexedSeq
  private def chunkRuns(xs: IndexedSeq[TagType]): List[IndexedSeq[TagType]] =
    import scala.collection.mutable.ListBuffer
    val out = ListBuffer.empty[IndexedSeq[TagType]]
    var i = 0
    val n = xs.length

    while (i < n) {
      xs(i) match
        case l @ TagType.Levelled(_) =>
          val start = i
          i += 1
          while (i < n && xs(i).isInstanceOf[TagType.Levelled]) i += 1
          out += xs.slice(start, i) // one run of bullets (Levelled)
        case s @ TagType.Single(_) =>
          out += IndexedSeq(s) // singleton chunk
          i += 1
    }
    out.toList
