package tagless

import munit.*
import tagless.dsl.*
import tags.T.*
import dom.*

class DslTest extends FunSuite:

  test("~ creates cursor from tag"):
    val cursor = ~div
    assertEquals(cursor.depth, 0)
    assertEquals(cursor.focus.value, Node.Element("div"))

  test(">> adds child and descends"):
    val cursor = ~div >> span
    assertEquals(cursor.depth, 1)
    assertEquals(cursor.focus.value, Node.Element("span"))

  test("> adds sibling"):
    val cursor = ~div >> span > p
    assertEquals(cursor.depth, 1)
    assertEquals(cursor.focus.value, Node.Element("p"))

  test(">>^ adds child without descending"):
    val inner = ~span
    val cursor = ~div >>^ Fragment(inner.seal)
    assertEquals(cursor.depth, 0)
    assertEquals(cursor.focus.children.length, 1)

  test("^ moves up one level"):
    val cursor = ~div >> span
    val parent = cursor.^
    assertEquals(parent.depth, 0)

  test("<^ moves up n levels"):
    val cursor = ~div >> ul >> li >> a
    assertEquals(cursor.depth, 3)
    val up2 = cursor <^ 2
    assertEquals(up2.depth, 1)

  test("tag with attributes"):
    val node = div(idAttr := "main", cls := "container")
    node match
      case Node.Element(tag, attrs) =>
        assertEquals(tag, "div")
        assertEquals(attrs.length, 2)
      case _ => fail("Expected Element")

  test("void tag creates VoidElement"):
    val node = br
    node match
      case Node.VoidElement(tag, _) =>
        assertEquals(tag, "br")
      case _ => fail("Expected VoidElement")

  test("tag with text content"):
    val node = p("Hello World")
    node match
      case Node.Element(tag, attrs) =>
        assertEquals(tag, "p")
        assert(attrs.exists {
          case AttrNode.Text("Hello World") => true
          case _ => false
        })
      case _ => fail("Expected Element")
