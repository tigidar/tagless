package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.render.PrettyHtml
import tags.TestI18n.{given, *}

class PrettyHtmlSpec extends FunSuite:

  // ============================================================
  // Basic elements
  // ============================================================

  test("empty div renders on one line"):
    assertEquals(
      (~div).toPrettyHtml,
      "<div></div>"
    )

  test("void element renders on one line"):
    assertEquals(
      (~img).toPrettyHtml,
      "<img />"
    )

  test("void element with attributes"):
    assertEquals(
      (~(img | "photo.jpg".src | "A photo".alt)).toPrettyHtml,
      """<img src="photo.jpg" alt="A photo" />"""
    )

  test("text-only element renders inline"):
    assertEquals(
      p("Hello".i18n).toPrettyHtml,
      "<p>Hello</p>"
    )

  test("text content is escaped"):
    assertEquals(
      p("<script>".i18n).toPrettyHtml,
      "<p>&lt;script&gt;</p>"
    )

  // ============================================================
  // Single child indentation
  // ============================================================

  test("element with one child element"):
    val result = (~div >>^ p("Hello".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <p>Hello</p>
         |</div>""".stripMargin
    )

  test("element with void child"):
    val result = (~div >>^ img).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <img />
         |</div>""".stripMargin
    )

  // ============================================================
  // Multiple children
  // ============================================================

  test("element with multiple children"):
    val result = (~div >>^ p("One".i18n) >>^ p("Two".i18n) >>^ p(
      "Three".i18n
    )).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <p>One</p>
         |  <p>Two</p>
         |  <p>Three</p>
         |</div>""".stripMargin
    )

  test("ul with li items"):
    val result = (~ul
      >>^ li("First".i18n)
      >>^ li("Second".i18n)
      >>^ li("Third".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<ul>
         |  <li>First</li>
         |  <li>Second</li>
         |  <li>Third</li>
         |</ul>""".stripMargin
    )

  // ============================================================
  // Deep nesting
  // ============================================================

  test("two levels deep"):
    val inner = ~ul >>^ li("Item".i18n)
    val result = (~div >>^ inner).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <ul>
         |    <li>Item</li>
         |  </ul>
         |</div>""".stripMargin
    )

  test("three levels deep"):
    val innerUl = ~ul >>^ li("Item".i18n)
    val innerSection = ~section >>^ innerUl
    val result = (~div >>^ innerSection).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <section>
         |    <ul>
         |      <li>Item</li>
         |    </ul>
         |  </section>
         |</div>""".stripMargin
    )

  // ============================================================
  // Attributes
  // ============================================================

  test("element with attributes and children"):
    val result = (~(div | "app".id | "container".cls)
      >>^ p("Hello".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<div id="app" class="container">
         |  <p>Hello</p>
         |</div>""".stripMargin
    )

  test("nested elements with attributes"):
    val innerUl = ~(ul | "menu".cls) >>^ li("Home".i18n) >>^ li("About".i18n)
    val result = (~(nav | "main-nav".id)
      >>^ innerUl).toPrettyHtml
    assertEquals(
      result,
      """|<nav id="main-nav">
         |  <ul class="menu">
         |    <li>Home</li>
         |    <li>About</li>
         |  </ul>
         |</nav>""".stripMargin
    )

  // ============================================================
  // Fragment rendering
  // ============================================================

  test("fragment renders children sequentially"):
    val frag = Fragment(
      Vector(
        Node.Element("p", Nil, Vector(Node.Text("One"))),
        Node.Element("p", Nil, Vector(Node.Text("Two")))
      )
    )
    val result = PrettyHtml.render(Node.Fragment(frag.nodes))
    assertEquals(
      result,
      """|<p>One</p>
         |<p>Two</p>""".stripMargin
    )

  test("fragment children inside element are indented"):
    val result =
      (~div >> Fragment.listItems("A".i18n, "B".i18n, "C".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<div>
         |  <li>A</li>
         |  <li>B</li>
         |  <li>C</li>
         |</div>""".stripMargin
    )

  // ============================================================
  // Mixed content
  // ============================================================

  test("mixed element and text children"):
    val node = Node.Element(
      "div",
      Nil,
      Vector(
        Node.Element("h1", Nil, Vector(Node.Text("Title"))),
        Node.Text("Some text"),
        Node.Element("p", Nil, Vector(Node.Text("Paragraph")))
      )
    )
    val result = PrettyHtml.render(node)
    assertEquals(
      result,
      """|<div>
         |  <h1>Title</h1>
         |  Some text
         |  <p>Paragraph</p>
         |</div>""".stripMargin
    )

  // ============================================================
  // Custom indent
  // ============================================================

  test("custom 4-space indent"):
    val result = (~div >>^ p("Hello".i18n)).toPrettyHtml("    ")
    assertEquals(
      result,
      """|<div>
         |    <p>Hello</p>
         |</div>""".stripMargin
    )

  test("tab indent"):
    val result = (~div >>^ p("Hello".i18n)).toPrettyHtml("\t")
    assertEquals(result, "<div>\n\t<p>Hello</p>\n</div>")

  test("custom indent nested"):
    val inner = ~ul >>^ li("Item".i18n)
    val result = (~div >>^ inner).toPrettyHtml("---")
    assertEquals(
      result,
      """|<div>
         |---<ul>
         |------<li>Item</li>
         |---</ul>
         |</div>""".stripMargin
    )

  // ============================================================
  // Tag extension
  // ============================================================

  test("tag toPrettyHtml renders empty element"):
    assertEquals(div.toPrettyHtml, "<div></div>")

  test("tag toPrettyHtml renders void element"):
    assertEquals(img.toPrettyHtml, "<img />")

  test("tag toPrettyHtml with custom indent"):
    assertEquals(div.toPrettyHtml("    "), "<div></div>")

  // ============================================================
  // PrettyHtml.render direct calls
  // ============================================================

  test("render Node.Text directly"):
    assertEquals(PrettyHtml.render(Node.Text("hello")), "hello")

  test("render Node.VoidElement directly"):
    assertEquals(
      PrettyHtml.render(Node.VoidElement("br", Nil)),
      "<br />"
    )

  test("render empty Node.Fragment"):
    assertEquals(PrettyHtml.render(Node.Fragment(Vector.empty)), "")

  test("render single-node Fragment"):
    assertEquals(
      PrettyHtml.render(Node.Fragment(Vector(Node.Text("hello")))),
      "hello"
    )

  // ============================================================
  // Complex realistic examples
  // ============================================================

  test("navigation with nested structure"):
    val liHome = ~li >>^ a("Home".i18n)
    val liAbout = ~li >>^ a("About".i18n)
    val liContact = ~li >>^ a("Contact".i18n)
    val innerUl = ~ul >>^ liHome >>^ liAbout >>^ liContact
    val innerNav = ~(nav | "main-nav".cls) >>^ innerUl
    val result = (~(header | "site-header".id)
      >>^ innerNav).toPrettyHtml
    assertEquals(
      result,
      """|<header id="site-header">
         |  <nav class="main-nav">
         |    <ul>
         |      <li>
         |        <a>Home</a>
         |      </li>
         |      <li>
         |        <a>About</a>
         |      </li>
         |      <li>
         |        <a>Contact</a>
         |      </li>
         |    </ul>
         |  </nav>
         |</header>""".stripMargin
    )

  test("form structure"):
    val nameField = ~div >>^ label("Name".i18n) >>^ input
    val emailField = ~div >>^ label("Email".i18n) >>^ input
    val result = (~form
      >>^ nameField
      >>^ emailField
      >>^ button("Submit".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<form>
         |  <div>
         |    <label>Name</label>
         |    <input />
         |  </div>
         |  <div>
         |    <label>Email</label>
         |    <input />
         |  </div>
         |  <button>Submit</button>
         |</form>""".stripMargin
    )

  test("details/summary with pretty print"):
    val result = (~details
      >>^ summary("Click to expand".i18n)
      >>^ p("Hidden content".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<details>
         |  <summary>Click to expand</summary>
         |  <p>Hidden content</p>
         |</details>""".stripMargin
    )

  // ============================================================
  // Boolean attributes
  // ============================================================

  test("boolean attributes render correctly"):
    val result = (~(details | Attr.Open)
      >>^ summary("Expanded".i18n)).toPrettyHtml
    assertEquals(
      result,
      """|<details open>
         |  <summary>Expanded</summary>
         |</details>""".stripMargin
    )

  // ============================================================
  // Consistency with Html renderer
  // ============================================================

  test("single text element matches Html renderer"):
    val cursor = p("Hello".i18n)
    assertEquals(cursor.toPrettyHtml, cursor.toHtml)

  test("empty element matches Html renderer"):
    assertEquals(div.toPrettyHtml, div.toHtml)

  test("void element matches Html renderer"):
    assertEquals(img.toPrettyHtml, img.toHtml)
