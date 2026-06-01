package tags.page

import tags.{Node, Attr, Cursor, Depth, ElementKind, Normal, Fragment}
import tags.meta.Meta
import tags.render.Html
import tags.i18n.{I18n, Lang}

// ============================================================
// Script loading mode
// ============================================================

enum ScriptMode derives CanEqual:
  case Default
  case Defer
  case Async
  case Module

// ============================================================
// Page data model
// ============================================================

final case class Stylesheet(href: String)

final case class Script(src: String, mode: ScriptMode = ScriptMode.Default)

final case class Page private (
    pageTitle: I18n[?],
    pageCharset: String,
    pageViewport: String,
    pageLang: Option[String],
    pageStylesheets: Vector[Stylesheet],
    pageScripts: Vector[Script],
    pageHeadNodes: Vector[Node],
    pageBodyContent: Option[Node]
):

  def charset(value: String): Page =
    copy(pageCharset = value)

  def viewport(value: String): Page =
    copy(pageViewport = value)

  def lang(value: String): Page =
    copy(pageLang = Some(value))

  def stylesheet(href: String): Page =
    copy(pageStylesheets = pageStylesheets :+ Stylesheet(href))

  def stylesheets(hrefs: String*): Page =
    copy(pageStylesheets = pageStylesheets ++ hrefs.map(Stylesheet(_)))

  def script(src: String): Page =
    copy(pageScripts = pageScripts :+ Script(src))

  def script(src: String, mode: ScriptMode): Page =
    copy(pageScripts = pageScripts :+ Script(src, mode))

  def scripts(srcs: String*): Page =
    copy(pageScripts = pageScripts ++ srcs.map(Script(_)))

  def headExtra[D <: Depth, K <: ElementKind](cursor: Cursor[D, K]): Page =
    copy(pageHeadNodes = pageHeadNodes :+ cursor.seal)

  def headExtra(node: Node): Page =
    copy(pageHeadNodes = pageHeadNodes :+ node)

  def body[D <: Depth, K <: ElementKind](cursor: Cursor[D, K]): Page =
    copy(pageBodyContent = Some(cursor.seal))

  def body(node: Node): Page =
    copy(pageBodyContent = Some(node))

  def body(fragment: Fragment): Page =
    copy(pageBodyContent = Some(Node.Fragment(fragment.nodes)))

object Page:
  def apply(title: I18n[?]): Page = Page(
    pageTitle = title,
    pageCharset = "utf-8",
    pageViewport = "width=device-width, initial-scale=1",
    pageLang = None,
    pageStylesheets = Vector.empty,
    pageScripts = Vector.empty,
    pageHeadNodes = Vector.empty,
    pageBodyContent = None
  )

// ============================================================
// Interpreter: Page → Node + HTML
// ============================================================

object PageInterpreter:

  /** Convert a Page to a Node.Element representing <html> with <head> and
    * <body>
    */
  def toNode(page: Page)(using Lang): Node.Element =
    val headChildren = buildHead(page)
    val headEl = Node.Element("head", Nil, headChildren)

    val bodyChildren = page.pageBodyContent match
      case Some(Node.Fragment(nodes)) => nodes
      case Some(node)                 => Vector(node)
      case None                       => Vector.empty
    val bodyEl = Node.Element("body", Nil, bodyChildren)

    val htmlAttrs = page.pageLang.map(l => Attr.Lang(l)).toList
    Node.Element("html", htmlAttrs, Vector(headEl, bodyEl))

  /** Render a Page to a complete HTML document string with <!DOCTYPE html>
    * prefix
    */
  def toHtml(page: Page)(using Lang): String =
    "<!DOCTYPE html>" + Html.render(toNode(page))

  private def buildHead(page: Page)(using lang: Lang): Vector[Node] =
    val nodes = Vector.newBuilder[Node]

    // Charset meta
    nodes += Meta.charset(page.pageCharset)

    // Viewport meta
    nodes += Meta.viewport(page.pageViewport)

    // Title
    nodes += Node.Element("title", Nil, Vector(Node.Text(page.pageTitle.apply)))

    // Stylesheets
    page.pageStylesheets.foreach { ss =>
      nodes += Node.VoidElement(
        "link",
        List(
          Attr.Rel("stylesheet"),
          Attr.Href(ss.href)
        )
      )
    }

    // Scripts
    page.pageScripts.foreach { s =>
      val attrs = List.newBuilder[Attr]
      attrs += Attr.Src(s.src)
      s.mode match
        case ScriptMode.Default => // no extra attr
        case ScriptMode.Defer   => attrs += Attr.Custom("defer", "")
        case ScriptMode.Async   => attrs += Attr.Custom("async", "")
        case ScriptMode.Module  => attrs += Attr.Type("module")
      nodes += Node.Element("script", attrs.result(), Vector.empty)
    }

    // Extra head nodes
    page.pageHeadNodes.foreach(n => nodes += n)

    nodes.result()

  /** Implicit conversion so Page can be used with >>^ in the main DSL */
  given pageToNode(using Lang): Conversion[Page, Node] = toNode(_)
