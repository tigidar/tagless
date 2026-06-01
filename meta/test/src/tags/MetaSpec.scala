package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.meta.Meta
import tags.render.Html

class MetaSpec extends FunSuite:

  // ============================================================
  // Node construction (data model tests)
  // ============================================================

  test("charset returns VoidElement with correct attrs"):
    val result = Meta.charset("utf-8")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(attrs, List(Attr.Custom("charset", "utf-8")))
      case _ =>
        fail("Expected VoidElement")

  test("viewport returns VoidElement with name and content attrs"):
    val result = Meta.viewport("width=device-width, initial-scale=1")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("viewport"),
            Attr.Custom("content", "width=device-width, initial-scale=1")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("description returns VoidElement with correct name and content"):
    val result = Meta.description("My awesome site")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("description"),
            Attr.Custom("content", "My awesome site")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("keywords joins args with comma-space separator"):
    val result = Meta.keywords("scala", "html", "dsl")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("keywords"),
            Attr.Custom("content", "scala, html, dsl")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("keywords with single keyword"):
    val result = Meta.keywords("scala")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("keywords"),
            Attr.Custom("content", "scala")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("author returns VoidElement with correct name and content"):
    val result = Meta.author("Jane Doe")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("author"),
            Attr.Custom("content", "Jane Doe")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("robots joins directives with comma-space separator"):
    val result = Meta.robots("noindex", "nofollow")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("robots"),
            Attr.Custom("content", "noindex, nofollow")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("robots with single directive"):
    val result = Meta.robots("noindex")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("robots"),
            Attr.Custom("content", "noindex")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("og uses property attribute (not name)"):
    val result = Meta.og("title", "My Page")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Custom("property", "og:title"),
            Attr.Custom("content", "My Page")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("twitter uses name attribute with twitter: prefix"):
    val result = Meta.twitter("card", "summary_large_image")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("twitter:card"),
            Attr.Custom("content", "summary_large_image")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  test("httpEquiv uses http-equiv attribute"):
    val result = Meta.httpEquiv("refresh", "30")
    result match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Custom("http-equiv", "refresh"),
            Attr.Custom("content", "30")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  // ============================================================
  // Rendered HTML output
  // ============================================================

  test("charset renders correctly"):
    val result = Html.render(Meta.charset("utf-8"))
    assertEquals(result, """<meta charset="utf-8" />""")

  test("viewport renders correctly"):
    val result =
      Html.render(Meta.viewport("width=device-width, initial-scale=1"))
    assertEquals(
      result,
      """<meta name="viewport" content="width=device-width, initial-scale=1" />"""
    )

  test("description renders correctly"):
    val result = Html.render(Meta.description("My site"))
    assertEquals(result, """<meta name="description" content="My site" />""")

  test("keywords renders correctly"):
    val result = Html.render(Meta.keywords("scala", "html", "dsl"))
    assertEquals(
      result,
      """<meta name="keywords" content="scala, html, dsl" />"""
    )

  test("author renders correctly"):
    val result = Html.render(Meta.author("Jane Doe"))
    assertEquals(result, """<meta name="author" content="Jane Doe" />""")

  test("robots renders correctly"):
    val result = Html.render(Meta.robots("noindex", "nofollow"))
    assertEquals(
      result,
      """<meta name="robots" content="noindex, nofollow" />"""
    )

  test("og renders correctly"):
    val result = Html.render(Meta.og("title", "My Page"))
    assertEquals(result, """<meta property="og:title" content="My Page" />""")

  test("twitter renders correctly"):
    val result = Html.render(Meta.twitter("card", "summary_large_image"))
    assertEquals(
      result,
      """<meta name="twitter:card" content="summary_large_image" />"""
    )

  test("httpEquiv renders correctly"):
    val result = Html.render(Meta.httpEquiv("refresh", "30"))
    assertEquals(result, """<meta http-equiv="refresh" content="30" />""")

  // ============================================================
  // defaults convenience
  // ============================================================

  test("defaults returns two elements"):
    val result = Meta.defaults
    assertEquals(result.length, 2)

  test("defaults first is charset utf-8"):
    val result = Meta.defaults
    val first = result.head
    first match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(attrs, List(Attr.Custom("charset", "utf-8")))
      case _ =>
        fail("Expected VoidElement")

  test("defaults second is viewport with standard responsive value"):
    val result = Meta.defaults
    val second = result(1)
    second match
      case Node.VoidElement(tag, attrs) =>
        assertEquals(tag, "meta")
        assertEquals(
          attrs,
          List(
            Attr.Name("viewport"),
            Attr.Custom("content", "width=device-width, initial-scale=1")
          )
        )
      case _ =>
        fail("Expected VoidElement")

  // ============================================================
  // DSL integration
  // ============================================================

  test("Meta node can be added as child via >>^"):
    val result = (~head >>^ Meta.description("Test")).toHtml
    assertEquals(
      result,
      """<head><meta name="description" content="Test" /></head>"""
    )

  test("Multiple Meta nodes can be chained"):
    val result = (~head
      >>^ Meta.charset("utf-8")
      >>^ Meta.viewport("width=device-width, initial-scale=1")
      >>^ Meta.description("Test site")).toHtml
    assertEquals(
      result,
      """<head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" /><meta name="description" content="Test site" /></head>"""
    )

  test("Meta.defaults can be folded into head"):
    val result = Meta.defaults
      .foldLeft(~head) { (cursor, metaNode) =>
        cursor >>^ metaNode
      }
      .toHtml
    assertEquals(
      result,
      """<head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" /></head>"""
    )

  // ============================================================
  // Realistic usage
  // ============================================================

  test("Complete head with multiple meta helpers combined"):
    val result = (~head
      >>^ Meta.charset("utf-8")
      >>^ Meta.viewport("width=device-width, initial-scale=1")
      >>^ Meta.description("A comprehensive example")
      >>^ Meta.keywords("scala", "functional", "html")
      >>^ Meta.author("John Smith")
      >>^ Node.Element("title", Nil, Vector(Node.Text("My Page")))).toHtml
    assertEquals(
      result,
      """<head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" /><meta name="description" content="A comprehensive example" /><meta name="keywords" content="scala, functional, html" /><meta name="author" content="John Smith" /><title>My Page</title></head>"""
    )

  test("Open Graph set (og title, description, image, url)"):
    val result = (~head
      >>^ Meta.og("title", "My Awesome Article")
      >>^ Meta.og("description", "This is an amazing article about Scala")
      >>^ Meta.og("image", "https://example.com/image.jpg")
      >>^ Meta.og("url", "https://example.com/article")).toHtml
    assertEquals(
      result,
      """<head><meta property="og:title" content="My Awesome Article" /><meta property="og:description" content="This is an amazing article about Scala" /><meta property="og:image" content="https://example.com/image.jpg" /><meta property="og:url" content="https://example.com/article" /></head>"""
    )
