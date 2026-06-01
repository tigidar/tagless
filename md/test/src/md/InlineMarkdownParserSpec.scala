package md

import munit.*
import md.InlineMarkdown.*

class InlineMarkdownParserSpec extends FunSuite:

  // ============================================================
  // Parser tests
  // ============================================================

  test("parse plain text"):
    assertEquals(
      InlineMarkdownParser.parse("hello"),
      Text("hello")
    )

  test("parse empty string"):
    assertEquals(
      InlineMarkdownParser.parse(""),
      Text("")
    )

  test("parse bold"):
    assertEquals(
      InlineMarkdownParser.parse("**bold**"),
      Bold(Text("bold"))
    )

  test("parse italic"):
    assertEquals(
      InlineMarkdownParser.parse("*italic*"),
      Italic(Text("italic"))
    )

  test("parse inline code"):
    assertEquals(
      InlineMarkdownParser.parse("`code`"),
      Code("code")
    )

  test("parse text followed by bold"):
    assertEquals(
      InlineMarkdownParser.parse("hello **world**"),
      Sequence(Vector(Text("hello "), Bold(Text("world"))))
    )

  test("parse bold followed by text and italic"):
    assertEquals(
      InlineMarkdownParser.parse("**bold** and *italic*"),
      Sequence(
        Vector(Bold(Text("bold")), Text(" and "), Italic(Text("italic")))
      )
    )

  test("parse mixed bold, text, and code"):
    assertEquals(
      InlineMarkdownParser.parse("use **this** or `that`"),
      Sequence(
        Vector(
          Text("use "),
          Bold(Text("this")),
          Text(" or "),
          Code("that")
        )
      )
    )

  test("parse unclosed bold as literal asterisks"):
    assertEquals(
      InlineMarkdownParser.parse("**unclosed"),
      Text("**unclosed")
    )

  test("parse unclosed italic as literal asterisk"):
    assertEquals(
      InlineMarkdownParser.parse("*unclosed"),
      Text("*unclosed")
    )

  test("parse unclosed backtick as literal"):
    assertEquals(
      InlineMarkdownParser.parse("`unclosed"),
      Text("`unclosed")
    )

  // ============================================================
  // Underline tests
  // ============================================================

  test("parse underline"):
    assertEquals(
      InlineMarkdownParser.parse("__underlined__"),
      Underline(Text("underlined"))
    )

  test("parse text with underline in middle"):
    assertEquals(
      InlineMarkdownParser.parse("text __underlined__ more"),
      Sequence(
        Vector(Text("text "), Underline(Text("underlined")), Text(" more"))
      )
    )

  test("parse unclosed underline as literal"):
    assertEquals(
      InlineMarkdownParser.parse("unclosed __text"),
      Text("unclosed __text")
    )

  test("parse bold inside underline"):
    assertEquals(
      InlineMarkdownParser.parse("__**bold inside**__"),
      Underline(Bold(Text("bold inside")))
    )

  // ============================================================
  // md"" interpolator tests
  // ============================================================

  test("md interpolator with plain text"):
    assertEquals(
      md"hello",
      Text("hello")
    )

  test("md interpolator with bold syntax"):
    assertEquals(
      md"hello **world**",
      Sequence(Vector(Text("hello "), Bold(Text("world"))))
    )

  test("md interpolator with typed splice"):
    assertEquals(
      md"The ${"cell body".bold} sums up signals",
      Sequence(
        Vector(Text("The "), Bold(Text("cell body")), Text(" sums up signals"))
      )
    )

  test("md interpolator with multiple splices"):
    val b = "bold".bold
    val i = "italic".italic
    assertEquals(
      md"Start $b middle $i end",
      Sequence(
        Vector(
          Text("Start "),
          Bold(Text("bold")),
          Text(" middle "),
          Italic(Text("italic")),
          Text(" end")
        )
      )
    )

  test("md interpolator splice only"):
    assertEquals(
      md"${"test".bold}",
      Bold(Text("test"))
    )

  test("md interpolator with markdown syntax and splice"):
    assertEquals(
      md"**hello** ${"world".italic}",
      Sequence(Vector(Bold(Text("hello")), Text(" "), Italic(Text("world"))))
    )

  // ============================================================
  // Math tests
  // ============================================================

  test("parse inline math with $...$"):
    assertEquals(
      InlineMarkdownParser.parse("The formula $E=mc^2$ is famous"),
      Sequence(Vector(Text("The formula "), Math("E=mc^2"), Text(" is famous")))
    )

  test("parse display math with $$...$$"):
    assertEquals(
      InlineMarkdownParser.parse("$$\\int_0^1 f(x) dx$$"),
      MathBlock("\\int_0^1 f(x) dx")
    )

  test("parse unclosed $ as literal"):
    assertEquals(
      InlineMarkdownParser.parse("costs $5"),
      Text("costs $5")
    )

  test("math extension creates Math variant"):
    assertEquals(
      "E=mc^2".math,
      Math("E=mc^2")
    )

  test("mathBlock extension creates MathBlock variant"):
    assertEquals(
      "\\sum_{i=1}^n x_i".mathBlock,
      MathBlock("\\sum_{i=1}^n x_i")
    )

  test("Math renders to span with katex class"):
    val html = tags.render.Html.render(InlineMarkdown.render(Math("x^2")))
    assert(
      html.contains("""class="katex-inline""""),
      s"Missing katex-inline class in: $html"
    )
    assert(html.contains("x^2"), s"Expected math text in: $html")

  test("MathBlock renders to div with katex-display class"):
    val html =
      tags.render.Html.render(InlineMarkdown.render(MathBlock("\\frac{1}{2}")))
    assert(
      html.contains("""class="katex-display""""),
      s"Missing katex-display class in: $html"
    )

  test("md interpolator with math splice"):
    val formula = "x^2 + y^2 = r^2".math
    assertEquals(
      md"The equation $formula describes a circle",
      Sequence(
        Vector(
          Text("The equation "),
          Math("x^2 + y^2 = r^2"),
          Text(" describes a circle")
        )
      )
    )

  test("md interpolator with $$ escaping for inline math"):
    assertEquals(
      md"Result: $$E=mc^2$$ done",
      Sequence(Vector(Text("Result: "), Math("E=mc^2"), Text(" done")))
    )

  test("md interpolator with $$$$ escaping for display math"):
    assertEquals(
      md"$$$$x^2 + 1$$$$",
      MathBlock("x^2 + 1")
    )

  // ============================================================
  // Image tests
  // ============================================================

  test("Image extension creates Image variant"):
    assertEquals(
      "photo.jpg".img("A photo"),
      Image("photo.jpg", "A photo")
    )

  test("Image renders to void element"):
    val node = InlineMarkdown.render(Image("photo.jpg", "A photo"))
    node match
      case tags.Node.VoidElement("img", attrs) =>
        assert(
          attrs.contains(tags.Attr.Src("photo.jpg")),
          s"Missing src attr in: $attrs"
        )
        assert(
          attrs.contains(tags.Attr.Alt("A photo")),
          s"Missing alt attr in: $attrs"
        )
      case _ => fail(s"Expected VoidElement img, got: $node")
