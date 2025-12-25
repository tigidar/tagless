package md

import munit.*

class MarkdownTest extends FunSuite:

  test("^ creates Init"):
    val m = Markdown.^
    assertEquals(m, Markdown.Init)

  test("<# creates H1"):
    val m = Markdown.^ <# "Title"
    m match
      case Markdown.Append(Markdown.Init, Markdown.H(value, level)) =>
        assertEquals(value, "Title")
        assertEquals(level, 1)
      case _ => fail("Expected Append with H1")

  test("<## creates H2"):
    val m = Markdown.^ <## "Subtitle"
    m match
      case Markdown.Append(Markdown.Init, Markdown.H(value, level)) =>
        assertEquals(value, "Subtitle")
        assertEquals(level, 2)
      case _ => fail("Expected Append with H2")

  test("< creates Paragraph"):
    val m = Markdown.^ < "Some text"
    m match
      case Markdown.Append(Markdown.Init, Markdown.Paragraph(s)) =>
        assertEquals(s, "Some text")
      case _ => fail("Expected Append with Paragraph")

  test("<* creates Star bullet"):
    val m = Markdown.^ <* "Item"
    m match
      case Markdown.Append(Markdown.Init, Markdown.Bullet(BulletType.Star(v))) =>
        assertEquals(v, "Item")
      case _ => fail("Expected Star bullet")

  test("<-- creates Dash bullet"):
    val m = Markdown.^ <-- "Dashed"
    m match
      case Markdown.Append(Markdown.Init, Markdown.Bullet(BulletType.Dash(v))) =>
        assertEquals(v, "Dashed")
      case _ => fail("Expected Dash bullet")

  test("<+ creates Numbered bullet with incrementing level"):
    val m = Markdown.^
      <+ "First"
      <+ "Second"
    m match
      case Markdown.Append(
        Markdown.Append(Markdown.Init, Markdown.Bullet(BulletType.Numbered(v1, l1))),
        Markdown.Bullet(BulletType.Numbered(v2, l2))
      ) =>
        assertEquals(v1, "First")
        assertEquals(l1, 1)
        assertEquals(v2, "Second")
        assertEquals(l2, 2)
      case _ => fail("Expected two numbered bullets")

  test("chaining multiple elements"):
    val m = Markdown.^
      <# "Header"
       < "Paragraph"
      <* "Bullet"

    val linear = TagType.linear(m)
    assertEquals(linear.length, 3)
