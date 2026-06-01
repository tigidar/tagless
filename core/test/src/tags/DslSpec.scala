package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.TestI18n.{given, *}

class DslSpec extends FunSuite:

  // ============================================================
  // Basic tag rendering
  // ============================================================

  test("empty div renders correctly"):
    assertEquals(div.toHtml, "<div></div>")

  test("void element renders self-closing"):
    assertEquals(img.toHtml, "<img />")

  test("tag with text content via apply"):
    // p("Hello".i18n) uses the apply extension which creates a cursor with text child
    assertEquals(p("Hello".i18n).toHtml, "<p>Hello</p>")

  // ============================================================
  // Attributes
  // ============================================================

  test("single attribute"):
    val result = (div | "app".id).toHtml
    assertEquals(result, """<div id="app"></div>""")

  test("multiple attributes"):
    val result = (div | "app".id | "container".cls).toHtml
    assertEquals(result, """<div id="app" class="container"></div>""")

  test("void element with attributes"):
    val result = (img | "photo.jpg".src | "A photo".alt).toHtml
    assertEquals(result, """<img src="photo.jpg" alt="A photo" />""")

  // ============================================================
  // ~ prefix operator (alias for toCursor)
  // ============================================================

  test("~ prefix operator creates cursor equivalent to toCursor"):
    assertEquals((~div).toHtml, div.toCursor.toHtml)

  test("~ prefix operator works with void elements"):
    assertEquals((~img).toHtml, img.toCursor.toHtml)

  test("~ prefix operator chains with child operator"):
    val result = (~div >> p).^.toHtml
    assertEquals(result, "<div><p></p></div>")

  // ============================================================
  // Child operations (>>)
  // ============================================================

  test("single child with descent and ascent"):
    // ~div >> p creates div with p child, cursor at depth 1
    // .^ ascends back to div at depth 0
    val cursor = ~div >> p
    val result = cursor.^.toHtml
    assertEquals(result, "<div><p></p></div>")

  test("child with text using >>^"):
    // >>^ adds child but stays at current depth
    val result = (~div >>^ p("Hello".i18n)).toHtml
    assertEquals(result, "<div><p>Hello</p></div>")

  test("nested children"):
    // ~div >> p creates cursor at p (depth 1)
    // >> span creates cursor at span (depth 2)
    // ^^ ascends 2 levels back to div (depth 0)
    val cursor = ~div >> p >> span
    val result = cursor.^^.toHtml
    assertEquals(result, "<div><p><span></span></p></div>")

  test("multiple children with >>^"):
    val result = (~div >>^ p >>^ span).toHtml
    assertEquals(result, "<div><p></p><span></span></div>")

  test("text child"):
    val cursor = ~div >> "Hello".i18n
    val result = cursor.^.toHtml
    assertEquals(result, "<div>Hello</div>")

  // ============================================================
  // Sibling operations (>)
  // ============================================================

  test("siblings with move"):
    // Start: ~ul >> li puts us at li (depth 1)
    // > li adds sibling li, moves focus to it (still depth 1)
    // > li adds another sibling (still depth 1)
    // ^ ascends to ul (depth 0)
    val result = (~ul >> li > li > li).^.toHtml
    assertEquals(result, "<ul><li></li><li></li><li></li></ul>")

  test("sibling stay (>^)"):
    // >> li puts us at first li (depth 1)
    // >^ li adds sibling but stays at first li
    // ^ ascends to ul
    val result = (~ul >> li >^ li).^.toHtml
    assertEquals(result, "<ul><li></li><li></li></ul>")

  // ============================================================
  // Fragment operations
  // ============================================================

  test("tag multiplication"):
    val result = (~ul >> (li * 3)).toHtml
    assertEquals(result, "<ul><li></li><li></li><li></li></ul>")

  test("fragment combination"):
    val frag = (p * 2) + (span * 2)
    val result = (~div >> frag).toHtml
    assertEquals(result, "<div><p></p><p></p><span></span><span></span></div>")

  // ============================================================
  // Collection-based fragment builders
  // ============================================================

  test("Fragment.from with tags"):
    val names = List("Alice", "Bob", "Carol").map(_.i18n)
    val frag = Fragment.from(names)(name => li(name))
    val result = (~ul >> frag).toHtml
    assertEquals(result, "<ul><li>Alice</li><li>Bob</li><li>Carol</li></ul>")

  test("Fragment.from with tags and attributes"):
    val items = List(("item1", "First".i18n), ("item2", "Second".i18n))
    val frag = Fragment.from(items) { case (id, text) =>
      (li | id.id)(text)
    }
    val result = (~ul >> frag).toHtml
    assertEquals(
      result,
      """<ul><li id="item1">First</li><li id="item2">Second</li></ul>"""
    )

  test("Fragment.from with cursors"):
    val names = List("Alice", "Bob").map(_.i18n)
    val frag = Fragment.from(names) { name =>
      (~li >> span(name)).^
    }
    val result = (~ul >> frag).toHtml
    assertEquals(
      result,
      "<ul><li><span>Alice</span></li><li><span>Bob</span></li></ul>"
    )

  test("Fragment.indexed creates numbered items"):
    val frag = Fragment.indexed(3)(i => li(s"Item $i".i18n))
    val result = (~ul >> frag).toHtml
    assertEquals(
      result,
      "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>"
    )

  test("Fragment.indexed with zero"):
    val frag = Fragment.indexed(0)(_ => li)
    assertEquals(frag.nodes.size, 0)

  test("toFragment extension on List"):
    val result =
      (~ul >> List("A", "B", "C").map(_.i18n).toFragment(li(_))).toHtml
    assertEquals(result, "<ul><li>A</li><li>B</li><li>C</li></ul>")

  test("toFragment with void elements"):
    val paths = List("/a.css", "/b.css")
    val frag = paths.toFragment(path => link | path.href)
    val result = (~head >> frag).toHtml
    assert(result.contains("""href="/a.css""""), s"Missing /a.css in: $result")
    assert(result.contains("""href="/b.css""""), s"Missing /b.css in: $result")

  // ============================================================
  // HTML Pattern Helpers
  // ============================================================

  test("Fragment.stylesheets with full paths"):
    val result =
      (~head >> Fragment.stylesheets("/css/app.css", "/css/vendor.css")).toHtml
    assert(
      result.contains("""rel="stylesheet""""),
      s"Missing rel=stylesheet in: $result"
    )
    assert(
      result.contains("""href="/css/app.css""""),
      s"Missing app.css in: $result"
    )
    assert(
      result.contains("""href="/css/vendor.css""""),
      s"Missing vendor.css in: $result"
    )

  test("Fragment.stylesheets with prefix"):
    val result = (~head >> Fragment.stylesheets("/css/")(
      "flatten",
      "layout",
      "styles"
    )).toHtml
    assert(
      result.contains("""href="/css/flatten.css""""),
      s"Missing flatten.css in: $result"
    )
    assert(
      result.contains("""href="/css/layout.css""""),
      s"Missing layout.css in: $result"
    )
    assert(
      result.contains("""href="/css/styles.css""""),
      s"Missing styles.css in: $result"
    )

  test("Fragment.options with value/text pairs"):
    val result = (~select >> Fragment.options(
      ("light", "Light".i18n),
      ("dark", "Dark".i18n)
    )).toHtml
    assertEquals(
      result,
      """<select><option value="light">Light</option><option value="dark">Dark</option></select>"""
    )

  test("Fragment.simpleOptions"):
    val result = (~select >> Fragment.simpleOptions(
      "System".i18n,
      "Light".i18n,
      "Dark".i18n
    )).toHtml
    assertEquals(
      result,
      "<select><option>System</option><option>Light</option><option>Dark</option></select>"
    )

  test("Fragment.navItems"):
    val result = (~ul >> Fragment.navItems(
      "/" -> "Home".i18n,
      "/about" -> "About".i18n
    )).toHtml
    assert(result.contains("""href="/""""), s"Missing / href in: $result")
    assert(result.contains(">Home</a>"), s"Missing Home text in: $result")
    assert(
      result.contains("""href="/about""""),
      s"Missing /about href in: $result"
    )

  test("Fragment.navItems creates li > a elements with i18n text"):
    import tags.i18n.*
    val en = Lang("en")
    val home = I18n.of["home"]("home", en -> "Home")
    val about = I18n.of["about"]("about", en -> "About")

    given Lang = en
    val result = (~ul >> Fragment.navItems(
      "#home" -> home,
      "#about" -> about
    )).toHtml

    assertEquals(
      result,
      """<ul><li><a href="#home">Home</a></li><li><a href="#about">About</a></li></ul>"""
    )

  test("Fragment.listItems"):
    val result = (~ul >> Fragment.listItems(
      "First".i18n,
      "Second".i18n,
      "Third".i18n
    )).toHtml
    assertEquals(result, "<ul><li>First</li><li>Second</li><li>Third</li></ul>")

  test("Fragment.hiddenSection"):
    val result =
      (~Fragment.hiddenSection("welcome") >>^ h1("Hello".i18n)).toHtml
    assert(
      result.contains("""id="welcome""""),
      s"Missing id=welcome in: $result"
    )
    assert(
      result.contains("""class="is-hidden""""),
      s"Missing is-hidden class in: $result"
    )
    assert(result.contains("<h1>Hello</h1>"), s"Missing h1 in: $result")

  // ============================================================
  // Complex examples
  // ============================================================

  test("navigation menu using >>^"):
    // Build using >>^ to add children without descending
    val result = (
      ~(nav | "main".id) >>
        (~ul >>^ li >>^ li >>^ li)
    ).^.toHtml

    assertEquals(
      result,
      """<nav id="main"><ul><li></li><li></li><li></li></ul></nav>"""
    )

  test("form with mixed elements"):
    // Use >>^ to add multiple children at the same level
    val result = (
      ~form
        >>^ label
        >>^ input
        >>^ button
    ).toHtml

    assertEquals(
      result,
      """<form><label></label><input /><button></button></form>"""
    )

  // ============================================================
  // HTML escaping
  // ============================================================

  test("text content is escaped"):
    val result = (~div >> "<script>alert('xss')</script>".i18n).^.toHtml
    assertEquals(
      result,
      "<div>&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;</div>"
    )

  // ============================================================
  // Type safety (these should compile)
  // ============================================================

  test("descend into normal element compiles"):
    val _: Cursor[Succ[D0], Normal] = ~div >> p
    assert(true)

  test("ascend returns correct depth"):
    val c1: Cursor[Succ[D0], Normal] = ~div >> p
    val c0: Cursor[D0, Normal] = c1.^
    assert(true)

  test("sibling at depth > 0 compiles"):
    val c1: Cursor[Succ[D0], Normal] = ~ul >> li
    val c2: Cursor[Succ[D0], Normal] = c1 > li
    assert(true)

  test("double ascent compiles at depth 2"):
    val c2: Cursor[Succ[Succ[D0]], Normal] = ~div >> p >> span
    val c0: Cursor[D0, Normal] = c2.^^
    assert(true)

  // ============================================================
  // Interactive elements (T008)
  // ============================================================

  test("details renders"):
    assertEquals(details.toHtml, "<details></details>")

  test("summary renders"):
    assertEquals(summary.toHtml, "<summary></summary>")

  test("dialog renders"):
    assertEquals(dialog.toHtml, "<dialog></dialog>")

  test("details with summary and content"):
    val result = (~details
      >>^ summary("Click to expand".i18n)
      >>^ p("Hidden content".i18n)).toHtml
    assertEquals(
      result,
      "<details><summary>Click to expand</summary><p>Hidden content</p></details>"
    )

  test("details with open attribute"):
    val result = (~(details | Attr.Open)
      >>^ summary("Expanded".i18n)
      >>^ p("Visible content".i18n)).toHtml
    assertEquals(
      result,
      "<details open><summary>Expanded</summary><p>Visible content</p></details>"
    )

  test("dialog with open attribute"):
    val result = (~(dialog | Attr.Open)
      >>^ p("Dialog content".i18n)).toHtml
    assertEquals(result, "<dialog open><p>Dialog content</p></dialog>")
