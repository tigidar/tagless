package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.page.*
import tags.TestI18n.{given, *}

class PageSpec extends FunSuite:

  // ============================================================
  // Page construction defaults
  // ============================================================

  test("Page with title only uses default charset and viewport"):
    val page = Page("Hello".i18n)
    assertEquals(page.pageTitle, "Hello".i18n)
    assertEquals(page.pageCharset, "utf-8")
    assertEquals(page.pageViewport, "width=device-width, initial-scale=1")
    assertEquals(page.pageLang, None)
    assertEquals(page.pageStylesheets, Vector.empty)
    assertEquals(page.pageScripts, Vector.empty)
    assertEquals(page.pageHeadNodes, Vector.empty)
    assertEquals(page.pageBodyContent, None)

  test("Page.charset overrides default"):
    val page = Page("T".i18n).charset("iso-8859-1")
    assertEquals(page.pageCharset, "iso-8859-1")

  test("Page.viewport overrides default"):
    val page = Page("T".i18n).viewport("width=1024")
    assertEquals(page.pageViewport, "width=1024")

  test("Page.lang sets lang attribute"):
    val page = Page("T".i18n).lang("en")
    assertEquals(page.pageLang, Some("en"))

  // ============================================================
  // Stylesheets
  // ============================================================

  test("single stylesheet"):
    val page = Page("T".i18n).stylesheet("style.css")
    assertEquals(page.pageStylesheets, Vector(Stylesheet("style.css")))

  test("multiple stylesheets via varargs"):
    val page = Page("T".i18n).stylesheets("a.css", "b.css", "c.css")
    assertEquals(
      page.pageStylesheets.map(_.href),
      Vector("a.css", "b.css", "c.css")
    )

  test("chained stylesheet calls accumulate"):
    val page = Page("T".i18n).stylesheet("a.css").stylesheet("b.css")
    assertEquals(
      page.pageStylesheets.map(_.href),
      Vector("a.css", "b.css")
    )

  // ============================================================
  // Scripts
  // ============================================================

  test("single script"):
    val page = Page("T".i18n).script("app.js")
    assertEquals(page.pageScripts, Vector(Script("app.js")))

  test("script with defer mode"):
    val page = Page("T".i18n).script("app.js", ScriptMode.Defer)
    assertEquals(page.pageScripts, Vector(Script("app.js", ScriptMode.Defer)))

  test("script with async mode"):
    val page = Page("T".i18n).script("analytics.js", ScriptMode.Async)
    assertEquals(
      page.pageScripts,
      Vector(Script("analytics.js", ScriptMode.Async))
    )

  test("script with module mode"):
    val page = Page("T".i18n).script("main.js", ScriptMode.Module)
    assertEquals(page.pageScripts, Vector(Script("main.js", ScriptMode.Module)))

  test("multiple scripts via varargs"):
    val page = Page("T".i18n).scripts("a.js", "b.js")
    assertEquals(
      page.pageScripts.map(_.src),
      Vector("a.js", "b.js")
    )

  test("chained script calls accumulate"):
    val page = Page("T".i18n)
      .script("vendor.js")
      .script("app.js", ScriptMode.Defer)
    assertEquals(page.pageScripts.size, 2)
    assertEquals(page.pageScripts(0), Script("vendor.js"))
    assertEquals(page.pageScripts(1), Script("app.js", ScriptMode.Defer))

  // ============================================================
  // Body content
  // ============================================================

  test("body with cursor"):
    val content = (~div >> h1("Hello".i18n)).^
    val page = Page("T".i18n).body(content)
    assert(page.pageBodyContent.isDefined)

  test("body with node"):
    val node = Node.Element("div", Nil, Vector(Node.Text("hi")))
    val page = Page("T".i18n).body(node)
    assertEquals(page.pageBodyContent, Some(node))

  test("body with fragment"):
    val frag = Fragment(Vector(Node.Text("a"), Node.Text("b")))
    val page = Page("T".i18n).body(frag)
    assert(page.pageBodyContent.isDefined)

  // ============================================================
  // Head extra content
  // ============================================================

  test("headExtra with cursor"):
    val cursor = ~(meta | "author".attr("name") | "Jane".attr("content"))
    val page = Page("T".i18n).headExtra(cursor)
    assertEquals(page.pageHeadNodes.size, 1)

  test("headExtra with node"):
    val node = Node.VoidElement(
      "meta",
      List(Attr.Custom("name", "robots"), Attr.Custom("content", "noindex"))
    )
    val page = Page("T".i18n).headExtra(node)
    assertEquals(page.pageHeadNodes, Vector(node))

  test("chained headExtra accumulates"):
    val n1 = Node.VoidElement("meta", List(Attr.Custom("name", "a")))
    val n2 = Node.VoidElement("meta", List(Attr.Custom("name", "b")))
    val page = Page("T".i18n).headExtra(n1).headExtra(n2)
    assertEquals(page.pageHeadNodes.size, 2)

  // ============================================================
  // PageInterpreter.toNode
  // ============================================================

  test("toNode produces html element with head and body"):
    val node = PageInterpreter.toNode(Page("Test".i18n))
    node match
      case Node.Element("html", _, children) =>
        assertEquals(children.size, 2)
        assert(children(0).isInstanceOf[Node.Element])
        assert(children(1).isInstanceOf[Node.Element])
        val headEl = children(0).asInstanceOf[Node.Element]
        val bodyEl = children(1).asInstanceOf[Node.Element]
        assertEquals(headEl.tag, "head")
        assertEquals(bodyEl.tag, "body")
      case _ => fail("Expected html Element")

  test("toNode includes lang attribute on html when set"):
    val node = PageInterpreter.toNode(Page("T".i18n).lang("nb"))
    node match
      case Node.Element("html", attrs, _) =>
        assert(attrs.contains(Attr.Lang("nb")))
      case _ => fail("Expected html Element")

  test("toNode omits lang attribute when not set"):
    val node = PageInterpreter.toNode(Page("T".i18n))
    node match
      case Node.Element("html", attrs, _) =>
        assert(attrs.isEmpty)
      case _ => fail("Expected html Element")

  test("head contains charset meta, viewport meta, and title"):
    val node = PageInterpreter.toNode(Page("My Title".i18n))
    val headEl = node.children(0).asInstanceOf[Node.Element]
    val children = headEl.children

    // charset meta
    val charsetMeta = children(0).asInstanceOf[Node.VoidElement]
    assertEquals(charsetMeta.tag, "meta")
    assert(charsetMeta.attrs.contains(Attr.Custom("charset", "utf-8")))

    // viewport meta
    val viewportMeta = children(1).asInstanceOf[Node.VoidElement]
    assertEquals(viewportMeta.tag, "meta")
    assert(viewportMeta.attrs.contains(Attr.Name("viewport")))
    assert(
      viewportMeta.attrs.contains(
        Attr.Custom("content", "width=device-width, initial-scale=1")
      )
    )

    // title
    val titleEl = children(2).asInstanceOf[Node.Element]
    assertEquals(titleEl.tag, "title")
    assertEquals(titleEl.children, Vector(Node.Text("My Title")))

  test("head includes stylesheet link elements"):
    val page = Page("T".i18n).stylesheets("a.css", "b.css")
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    // charset (0), viewport (1), title (2), stylesheets (3, 4)
    val link1 = headEl.children(3).asInstanceOf[Node.VoidElement]
    assertEquals(link1.tag, "link")
    assert(link1.attrs.contains(Attr.Rel("stylesheet")))
    assert(link1.attrs.contains(Attr.Href("a.css")))

    val link2 = headEl.children(4).asInstanceOf[Node.VoidElement]
    assert(link2.attrs.contains(Attr.Href("b.css")))

  test("head includes script elements"):
    val page = Page("T".i18n).script("app.js")
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    // charset (0), viewport (1), title (2), script (3)
    val scriptEl = headEl.children(3).asInstanceOf[Node.Element]
    assertEquals(scriptEl.tag, "script")
    assert(scriptEl.attrs.contains(Attr.Src("app.js")))

  test("defer script has defer attribute"):
    val page = Page("T".i18n).script("app.js", ScriptMode.Defer)
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    val scriptEl = headEl.children(3).asInstanceOf[Node.Element]
    assert(scriptEl.attrs.contains(Attr.Custom("defer", "")))

  test("async script has async attribute"):
    val page = Page("T".i18n).script("app.js", ScriptMode.Async)
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    val scriptEl = headEl.children(3).asInstanceOf[Node.Element]
    assert(scriptEl.attrs.contains(Attr.Custom("async", "")))

  test("module script has type module attribute"):
    val page = Page("T".i18n).script("main.js", ScriptMode.Module)
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    val scriptEl = headEl.children(3).asInstanceOf[Node.Element]
    assert(scriptEl.attrs.contains(Attr.Type("module")))

  test("head includes extra nodes after default content"):
    val extraNode = Node.VoidElement(
      "meta",
      List(Attr.Custom("name", "robots"), Attr.Custom("content", "noindex"))
    )
    val page = Page("T".i18n).headExtra(extraNode)
    val node = PageInterpreter.toNode(page)
    val headEl = node.children(0).asInstanceOf[Node.Element]
    // charset (0), viewport (1), title (2), extra (3)
    assertEquals(headEl.children(3), extraNode)

  test("body contains provided cursor content"):
    val content = (~div >> h1("Hello".i18n)).^
    val page = Page("T".i18n).body(content)
    val node = PageInterpreter.toNode(page)
    val bodyEl = node.children(1).asInstanceOf[Node.Element]
    assertEquals(bodyEl.children.size, 1)
    val divEl = bodyEl.children(0).asInstanceOf[Node.Element]
    assertEquals(divEl.tag, "div")

  test("body is empty when no content provided"):
    val node = PageInterpreter.toNode(Page("T".i18n))
    val bodyEl = node.children(1).asInstanceOf[Node.Element]
    assertEquals(bodyEl.children, Vector.empty)

  test("body with fragment unwraps into children"):
    val frag = Fragment(
      Vector(
        Node.Element("p", Nil, Vector(Node.Text("one"))),
        Node.Element("p", Nil, Vector(Node.Text("two")))
      )
    )
    val page = Page("T".i18n).body(frag)
    val node = PageInterpreter.toNode(page)
    val bodyEl = node.children(1).asInstanceOf[Node.Element]
    assertEquals(bodyEl.children.size, 2)

  // ============================================================
  // PageInterpreter.toHtml (full document rendering)
  // ============================================================

  test("toHtml starts with DOCTYPE"):
    val html = PageInterpreter.toHtml(Page("Test".i18n))
    assert(html.startsWith("<!DOCTYPE html>"))

  test("toHtml renders complete minimal page"):
    val html = PageInterpreter.toHtml(Page("Hello".i18n))
    assert(html.startsWith("<!DOCTYPE html><html>"))
    assert(html.contains("<meta charset=\"utf-8\" />"))
    assert(
      html.contains(
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
      )
    )
    assert(html.contains("<title>Hello</title>"))
    assert(html.contains("<body></body>"))
    assert(html.endsWith("</html>"))

  test("toHtml renders page with lang"):
    val html = PageInterpreter.toHtml(Page("T".i18n).lang("en"))
    assert(html.contains("<html lang=\"en\">"))

  test("toHtml renders page with stylesheets"):
    val html = PageInterpreter.toHtml(
      Page("T".i18n).stylesheets("style.css", "theme.css")
    )
    assert(html.contains("""<link rel="stylesheet" href="style.css" />"""))
    assert(html.contains("""<link rel="stylesheet" href="theme.css" />"""))

  test("toHtml renders page with scripts"):
    val html = PageInterpreter.toHtml(Page("T".i18n).script("app.js"))
    assert(html.contains("""<script src="app.js"></script>"""))

  test("toHtml renders defer script"):
    val html =
      PageInterpreter.toHtml(Page("T".i18n).script("app.js", ScriptMode.Defer))
    assert(html.contains("""<script src="app.js" defer=""></script>"""))

  test("toHtml renders module script"):
    val html = PageInterpreter.toHtml(
      Page("T".i18n).script("main.js", ScriptMode.Module)
    )
    assert(html.contains("""<script src="main.js" type="module"></script>"""))

  test("toHtml renders page with body content"):
    val content = (~div >> h1("Welcome".i18n)).^
    val html = PageInterpreter.toHtml(
      Page("Test".i18n).body(content)
    )
    assert(html.contains("<body><div><h1>Welcome</h1></div></body>"))

  // ============================================================
  // Full integration: complete page
  // ============================================================

  test("complete page with all features"):
    val bodyContent = (~div >>^ header("My App".i18n) >>^ main("Content".i18n))
    val page = Page("My App".i18n)
      .lang("en")
      .charset("utf-8")
      .viewport("width=device-width, initial-scale=1")
      .stylesheets("reset.css", "app.css")
      .script("vendor.js")
      .script("app.js", ScriptMode.Defer)
      .script("main.js", ScriptMode.Module)
      .body(bodyContent)
    val html = PageInterpreter.toHtml(page)
    assert(html.startsWith("<!DOCTYPE html>"))
    assert(html.contains("""<html lang="en">"""))
    assert(html.contains("""<meta charset="utf-8" />"""))
    assert(html.contains("""<title>My App</title>"""))
    assert(html.contains("""<link rel="stylesheet" href="reset.css" />"""))
    assert(html.contains("""<link rel="stylesheet" href="app.css" />"""))
    assert(html.contains("""<script src="vendor.js"></script>"""))
    assert(html.contains("""<script src="app.js" defer=""></script>"""))
    assert(html.contains("""<script src="main.js" type="module"></script>"""))
    assert(html.contains("<body>"))
    assert(html.contains("</body>"))

  // ============================================================
  // Implicit conversion to Node
  // ============================================================

  test("Page converts to Node via given Conversion"):
    import tags.page.PageInterpreter.given
    import scala.language.implicitConversions

    val page = Page("T".i18n).body(p("Hi".i18n))
    val node: Node = page
    node match
      case Node.Element("html", _, children) =>
        assertEquals(children.size, 2)
      case _ => fail("Expected html Element from conversion")

  test("Page integrates with DSL via implicit conversion"):
    import tags.page.PageInterpreter.given
    import scala.language.implicitConversions

    val page = Page("Inner".i18n).body(p("Content".i18n))
    val wrapper = (~div >>^ (page: Node)).toHtml
    assert(wrapper.contains("<html>"))
    assert(wrapper.contains("<p>Content</p>"))
