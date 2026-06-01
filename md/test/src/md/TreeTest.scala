package md

import munit.*
import tags.dsl.*
import tags.TestI18n.{given, *}
import md.InlineMarkdown.*

class TreeTest extends FunSuite:

  test("create a very simple tree"):
    val root = Tree.builder(0)
    val one = root.addChild(1)
    val two = one.addChild(2)
    val three = two.addChild(3)
    val four = three.up(2).addChild(4)
    assertEquals(
      four.result,
      Tree.Node(
        0,
        Vector(
          Tree.Node(
            1,
            Vector(
              Tree.Node(
                2,
                Vector(
                  Tree.Node(3, Vector.empty)
                )
              ),
              // Add sibling 4 to 1
              Tree.Node(4, Vector.empty)
            )
          )
        )
      )
    )

  // ============================================================
  // Markdown DSL tests
  // ============================================================

  test("markdown with heading renders to HTML"):
    val md = Markdown.^ <# "Welcome".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<h1>Welcome</h1>"), s"Missing h1 in: $html")

  test("markdown with paragraph renders to HTML"):
    val md = Markdown.^ < "Hello world".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<p>Hello world</p>"), s"Missing p in: $html")

  test("markdown with multiple headings"):
    val md = Markdown.^
      <# "Title".i18n
      <## "Subtitle".i18n
      <### "Section".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<h1>Title</h1>"), s"Missing h1 in: $html")
    assert(html.contains("<h2>Subtitle</h2>"), s"Missing h2 in: $html")
    assert(html.contains("<h3>Section</h3>"), s"Missing h3 in: $html")

  test("markdown with star bullets renders ul with li"):
    val md = Markdown.^
      <* "Item 1".i18n
      <* "Item 2".i18n
      <* "Item 3".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<ul>"), s"Missing ul in: $html")
    assert(html.contains("<li>Item 1</li>"), s"Missing li 1 in: $html")
    assert(html.contains("<li>Item 2</li>"), s"Missing li 2 in: $html")
    assert(html.contains("<li>Item 3</li>"), s"Missing li 3 in: $html")
    assert(html.contains("</ul>"), s"Missing /ul in: $html")

  test("markdown with dash bullets has dash class"):
    val md = Markdown.^
      <-- "Dash item".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("""class="dash""""), s"Missing dash class in: $html")
    assert(html.contains("<li"), s"Missing li in: $html")

  test("markdown with numbered bullets"):
    val md = Markdown.^
      <+ "First".i18n
      <+ "Second".i18n
      <+ "Third".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<ul>"), s"Missing ul in: $html")
    assert(html.contains("<li>First</li>"), s"Missing li First in: $html")
    assert(html.contains("<li>Second</li>"), s"Missing li Second in: $html")

  test("markdown with mixed content"):
    val md = Markdown.^
      <# "Welcome".i18n
      < "This is a paragraph.".i18n
      <* "Bullet one".i18n
      <* "Bullet two".i18n
      <## "Next Section".i18n
      < "Another paragraph.".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<h1>Welcome</h1>"), s"Missing h1 in: $html")
    assert(html.contains("<p>This is a paragraph.</p>"), s"Missing p in: $html")
    assert(html.contains("<ul>"), s"Missing ul in: $html")
    assert(html.contains("<li>Bullet one</li>"), s"Missing li in: $html")
    assert(html.contains("<h2>Next Section</h2>"), s"Missing h2 in: $html")

  test("markdown toHtml returns composable cursor"):
    import tags.dsl.*
    import tags.html.*

    val md = Markdown.^ <# "Blog Post".i18n < "Content here".i18n
    val page = ~article >>^ md.toHtml >>^ footer("End".i18n)
    val html = page.toHtml

    assert(html.contains("<article>"), s"Missing article in: $html")
    assert(html.contains("<h1>Blog Post</h1>"), s"Missing h1 in: $html")
    assert(html.contains("<footer>End</footer>"), s"Missing footer in: $html")

  test("all heading levels render correctly"):
    val md = Markdown.^
      <# "H1".i18n
      <## "H2".i18n
      <### "H3".i18n
      <#### "H4".i18n
      <##### "H5".i18n
      <###### "H6".i18n
    val html = md.toHtml.toHtml
    assert(html.contains("<h1>H1</h1>"), s"Missing h1 in: $html")
    assert(html.contains("<h2>H2</h2>"), s"Missing h2 in: $html")
    assert(html.contains("<h3>H3</h3>"), s"Missing h3 in: $html")
    assert(html.contains("<h4>H4</h4>"), s"Missing h4 in: $html")
    assert(html.contains("<h5>H5</h5>"), s"Missing h5 in: $html")
    assert(html.contains("<h6>H6</h6>"), s"Missing h6 in: $html")

  // ============================================================
  // Nested bullet depth tracking
  // ============================================================

  test("BulletType.Star tracks depth"):
    val star0 = BulletType.Star("test".i18n, 0)
    val star1 = BulletType.Star("test".i18n, 1)
    star0 match
      case BulletType.Star(_, d) => assertEquals(d, 0)
      case _                     => fail("Expected Star")
    star1 match
      case BulletType.Star(_, d) => assertEquals(d, 1)
      case _                     => fail("Expected Star")

  test("nested star bullet increases depth"):
    val md = Markdown.^
      <* "Level 0".i18n
      <*> "Level 1".i18n
    md match
      case Markdown.Append(_, Markdown.Bullet(BulletType.Star(_, d))) =>
        assertEquals(d, 1)
      case _ => fail("Expected nested bullet structure")

  // ============================================================
  // Blockquote support
  // ============================================================

  test("blockquote renders correctly"):
    val inner = Markdown.^ < "Quoted text".i18n
    val md = Markdown.^ <| inner
    val html = md.toHtml.toHtml
    assert(html.contains("<blockquote>"), s"Missing blockquote in: $html")
    assert(html.contains("<p>Quoted text</p>"), s"Missing p in blockquote in: $html")
    assert(html.contains("</blockquote>"), s"Missing /blockquote in: $html")

  test("blockquote with heading inside"):
    val inner = Markdown.^ <# "Quoted Heading".i18n
    val md = Markdown.^ <| inner
    val html = md.toHtml.toHtml
    assert(html.contains("<blockquote>"), s"Missing blockquote in: $html")
    assert(html.contains("<h1>Quoted Heading</h1>"), s"Missing h1 in blockquote in: $html")

  // ============================================================
  // Code block support
  // ============================================================

  test("code block without language renders pre>code"):
    val md = Markdown.^ << "val x = 1"
    val html = md.toHtml.toHtml
    assert(html.contains("<pre>"), s"Missing pre in: $html")
    assert(html.contains("<code>val x = 1</code>"), s"Missing code in: $html")
    assert(html.contains("</pre>"), s"Missing /pre in: $html")

  test("code block with language has class"):
    val md = Markdown.^ <<("val x = 1", "scala")
    val html = md.toHtml.toHtml
    assert(html.contains("""class="language-scala""""), s"Missing language class in: $html")
    assert(html.contains("val x = 1"), s"Missing code content in: $html")

  // ============================================================
  // InlineMarkdown support
  // ============================================================

  test("InlineMarkdown.text creates Text"):
    val inline = InlineMarkdown.text("Hello")
    inline match
      case InlineMarkdown.Text("Hello") => // pass
      case _ => fail("Expected Text")

  test("InlineMarkdown bold extension"):
    val inline = InlineMarkdown.text("test").bold
    inline match
      case InlineMarkdown.Bold(InlineMarkdown.Text("test")) => // pass
      case _ => fail("Expected Bold")

  test("InlineMarkdown italic extension"):
    val inline = InlineMarkdown.text("test").italic
    inline match
      case InlineMarkdown.Italic(InlineMarkdown.Text("test")) => // pass
      case _ => fail("Expected Italic")

  test("InlineMarkdown + combines"):
    val inline = InlineMarkdown.text("Hello ") + InlineMarkdown.text("World").bold
    inline match
      case InlineMarkdown.Sequence(Vector(InlineMarkdown.Text("Hello "), InlineMarkdown.Bold(InlineMarkdown.Text("World")))) => // pass
      case _ => fail(s"Expected Sequence, got $inline")

  test("InlineMarkdown.render produces correct nodes"):
    val inline = InlineMarkdown.text("Hello").bold
    val node = InlineMarkdown.render(inline)
    node match
      case tags.Node.Element("strong", Nil, Vector(tags.Node.Text("Hello"))) => // pass
      case _ => fail(s"Unexpected node: $node")

  test("I18n toInline creates InlineMarkdown"):
    val inline = "Hello".i18n.toInline
    inline match
      case InlineMarkdown.Text("Hello") => // pass
      case _ => fail("Expected Text")

  test("I18n inlineBold creates bold inline"):
    val inline = "Hello".i18n.inlineBold
    inline match
      case InlineMarkdown.Bold(InlineMarkdown.Text("Hello")) => // pass
      case _ => fail("Expected Bold")

  test("rich paragraph with inline formatting renders correctly"):
    val inline = "Hello ".i18n.toInline + "World".i18n.inlineBold
    val md = Markdown.^ <~ inline
    val html = md.toHtml.toHtml
    assert(html.contains("<p>"), s"Missing p in: $html")
    assert(html.contains("Hello "), s"Missing Hello in: $html")
    assert(html.contains("<strong>World</strong>"), s"Missing strong in: $html")
