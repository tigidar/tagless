package md

import dom.{Cursor, NodeType, NormalType}

enum BulletType:
  case Star(value: String)
  case Dash(value: String)
  case Numbered(value: String, level: Int)

// format: off
enum Markdown derives CanEqual:
  self =>
  case Init
  case H(value: String, level: Int)
  case Bullet(tpe: BulletType)
  case Paragraph(s: String)
  case Append(l: Markdown, r: Markdown)

  def <(m: String): Markdown =
    Append(self, Paragraph(m))

  def <*(s: String): Markdown =
    Append(self, Bullet(BulletType.Star(s)))

  def <+(s: String): Markdown =
    self match
      case Bullet(BulletType.Numbered(_,n)) =>
        Append(self, Bullet(BulletType.Numbered(s,n + 1)))
      case _ =>
        Append(self, Bullet(BulletType.Numbered(s,1)))

  def <--(s: String): Markdown =
    Append(self, Bullet(BulletType.Dash(s)))

  def <# (s: String): Markdown =
    Append(self, H(s,1))

  def <## (s: String): Markdown =
    Append(self, H(s,2))

  def <### (s: String): Markdown =
    Append(self, H(s,3))
  
  def <#### (s: String): Markdown =
    Append(self, H(s,4))

  def <##### (s: String): Markdown =
    Append(self, H(s,5))

  def <###### (s: String): Markdown =
    Append(self, H(s,6))

  def toDom[N <: NodeType, E <: tags.gen.Element](c: Cursor[N,E])(using ev: N =:= NormalType): Cursor[N,E] =
    MarkdownConverter.build(self, c)

object Markdown:
  def ^ = Markdown.Init

