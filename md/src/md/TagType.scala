package md

import Markdown.*

enum TagType:
  case Single(md: H | Paragraph)
  case Levelled(md: BulletType, depth: Int)
  case Block(md: Blockquote)
  case Code(md: CodeBlock)
  case Rich(md: RichParagraph)

object TagType:
  // 1) Flatten Markdown -> linear stream of TagType
  def linear(md: Markdown): IndexedSeq[TagType] =
    md match
      case Append(l, r) => linear(l) ++ linear(r)
      case Bullet(bt) =>
        val depth = bt match
          case BulletType.Star(_, d)        => d
          case BulletType.Dash(_, d)        => d
          case BulletType.Numbered(_, _, d) => d
        IndexedSeq(TagType.Levelled(bt, depth))
      case h: H              => IndexedSeq(TagType.Single(h))
      case p: Paragraph      => IndexedSeq(TagType.Single(p))
      case rp: RichParagraph => IndexedSeq(TagType.Rich(rp))
      case bq: Blockquote    => IndexedSeq(TagType.Block(bq))
      case cb: CodeBlock     => IndexedSeq(TagType.Code(cb))
      case Init              => IndexedSeq.empty

  // 2) Group consecutive Levelled(...) into one chunk; Singles become singleton chunks
  def chunked(md: Markdown): List[IndexedSeq[TagType]] =
    chunkRuns(linear(md))

  // Pure, allocation-light chunker over an IndexedSeq
  // Groups consecutive Levelled items into one chunk (for list rendering)
  private def chunkRuns(xs: IndexedSeq[TagType]): List[IndexedSeq[TagType]] =
    import scala.collection.mutable.ListBuffer
    val out = ListBuffer.empty[IndexedSeq[TagType]]
    var i = 0
    val n = xs.length

    while (i < n) {
      xs(i) match
        case l @ TagType.Levelled(_, _) =>
          val start = i
          i += 1
          // Continue while still have Levelled items (any depth)
          while (i < n && xs(i).isInstanceOf[TagType.Levelled]) i += 1
          out += xs.slice(start, i) // one run of bullets
        case s @ TagType.Single(_) =>
          out += IndexedSeq(s) // singleton chunk
          i += 1
        case b @ TagType.Block(_) =>
          out += IndexedSeq(b) // singleton chunk
          i += 1
        case c @ TagType.Code(_) =>
          out += IndexedSeq(c) // singleton chunk
          i += 1
        case r @ TagType.Rich(_) =>
          out += IndexedSeq(r) // singleton chunk
          i += 1
    }
    out.toList
