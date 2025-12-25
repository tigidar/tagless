package md

import munit.*

class TagTypeTest extends FunSuite:

  test("linear flattens Init to empty"):
    val result = TagType.linear(Markdown.Init)
    assertEquals(result, IndexedSeq.empty)

  test("linear converts H to Single"):
    val m = Markdown.^ <# "Title"
    val result = TagType.linear(m)
    assertEquals(result.length, 1)
    result.head match
      case TagType.Single(Markdown.H(v, l)) =>
        assertEquals(v, "Title")
        assertEquals(l, 1)
      case _ => fail("Expected Single with H")

  test("linear converts Bullet to Levelled"):
    val m = Markdown.^ <* "Item"
    val result = TagType.linear(m)
    assertEquals(result.length, 1)
    result.head match
      case TagType.Levelled(BulletType.Star(v)) =>
        assertEquals(v, "Item")
      case _ => fail("Expected Levelled with Star")

  test("chunked groups consecutive bullets"):
    val m = Markdown.^
      <* "First"
      <* "Second"
      <* "Third"
    val chunks = TagType.chunked(m)
    assertEquals(chunks.length, 1)
    assertEquals(chunks.head.length, 3)

  test("chunked separates singles"):
    val m = Markdown.^
      <# "Header 1"
      <# "Header 2"
    val chunks = TagType.chunked(m)
    assertEquals(chunks.length, 2)
    assert(chunks.forall(_.length == 1))

  test("chunked groups bullets between singles"):
    val m = Markdown.^
      <# "Header"
      <* "Bullet 1"
      <* "Bullet 2"
       < "Paragraph"
    val chunks = TagType.chunked(m)
    assertEquals(chunks.length, 3)
    assertEquals(chunks(0).length, 1) // Header
    assertEquals(chunks(1).length, 2) // Two bullets
    assertEquals(chunks(2).length, 1) // Paragraph
