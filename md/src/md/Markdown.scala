package md

import tags.{Cursor, D0, Normal}
import tags.i18n.{I18n, Lang}

enum BulletType:
  case Star(value: I18n[?], depth: Int = 0)
  case Dash(value: I18n[?], depth: Int = 0)
  case Numbered(value: I18n[?], level: Int, depth: Int = 0)

// format: off
enum Markdown derives CanEqual:
  self =>
  case Init
  case H(value: I18n[?], level: Int)
  case Bullet(tpe: BulletType)
  case Paragraph(s: I18n[?])
  case RichParagraph(content: InlineMarkdown)
  case Blockquote(content: Markdown)
  case CodeBlock(code: String, language: Option[String] = None)
  case Append(l: Markdown, r: Markdown)

  def <[K <: String](m: I18n[K]): Markdown =
    Append(self, Paragraph(m))

  /** Add a rich paragraph with inline formatting */
  def <~(content: InlineMarkdown): Markdown =
    Append(self, RichParagraph(content))

  def <*[K <: String](s: I18n[K]): Markdown =
    Append(self, Bullet(BulletType.Star(s)))

  def <+[K <: String](s: I18n[K]): Markdown =
    self match
      case Bullet(BulletType.Numbered(_, n, _)) =>
        Append(self, Bullet(BulletType.Numbered(s, n + 1)))
      case _ =>
        Append(self, Bullet(BulletType.Numbered(s, 1)))

  def <--[K <: String](s: I18n[K]): Markdown =
    Append(self, Bullet(BulletType.Dash(s)))

  /** Add nested star bullet (depth increases based on previous bullet) */
  def <*>[K <: String](s: I18n[K]): Markdown =
    self match
      case Bullet(BulletType.Star(_, d)) => Append(self, Bullet(BulletType.Star(s, d + 1)))
      case _                             => Append(self, Bullet(BulletType.Star(s, 1)))

  /** Add nested dash bullet (depth increases based on previous bullet) */
  def <-->[K <: String](s: I18n[K]): Markdown =
    self match
      case Bullet(BulletType.Dash(_, d)) => Append(self, Bullet(BulletType.Dash(s, d + 1)))
      case _                             => Append(self, Bullet(BulletType.Dash(s, 1)))

  /** Add nested numbered bullet (depth increases based on previous bullet) */
  def <+>[K <: String](s: I18n[K]): Markdown =
    self match
      case Bullet(BulletType.Numbered(_, n, d)) => Append(self, Bullet(BulletType.Numbered(s, n + 1, d + 1)))
      case _                                    => Append(self, Bullet(BulletType.Numbered(s, 1, 1)))

  def <#[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,1))

  def <##[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,2))

  def <###[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,3))

  def <####[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,4))

  def <#####[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,5))

  def <######[K <: String](s: I18n[K]): Markdown =
    Append(self, H(s,6))

  /** Add a blockquote */
  def <|(content: Markdown): Markdown =
    Append(self, Blockquote(content))

  /** Add a code block without language */
  def <<(code: String): Markdown =
    Append(self, CodeBlock(code))

  /** Add a code block with language */
  def <<(code: String, lang: String): Markdown =
    Append(self, CodeBlock(code, Some(lang)))

  def toHtml(using Lang): Cursor[D0, Normal] =
    MarkdownConverter.build(self)

object Markdown:
  def ^ = Markdown.Init

