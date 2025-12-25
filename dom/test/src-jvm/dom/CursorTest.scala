package dom

import munit.*

class CursorTest extends FunSuite:

  test("cursor root has depth 0"):
    val cursor = Cursor.root
    assertEquals(cursor.depth, 0)

  test("cursor root isRoot is true"):
    val cursor = Cursor.root
    assert(cursor.isRoot)

  test("cursor root has no parent"):
    val cursor = Cursor.root
    assertEquals(cursor.parent, None)

  test("addChild increases depth"):
    val cursor = Cursor.root
    val child = cursor.addChild(Node.Element[NormalType, tags.gen.HTMLDivElement]("div"))
    assertEquals(child.depth, 1)

  test("addChild and up returns to original depth"):
    val cursor = Cursor.root
    val child = cursor.addChild(Node.Element[NormalType, tags.gen.HTMLDivElement]("div"))
    val parent = child.up
    assertEquals(parent.depth, 0)

  test("seal produces correct tree structure"):
    val cursor = Cursor.root
    val withChild = cursor.addChild(Node.Element[NormalType, tags.gen.HTMLDivElement]("div"))
    val tree = withChild.seal
    assertEquals(tree.value, Node.Element("html"))
    assertEquals(tree.children.length, 1)

  test("addChildStay keeps focus at parent"):
    val cursor = Cursor.root
    val after = cursor.addChildStay(Node.Element[NormalType, tags.gen.HTMLDivElement]("div"))
    assertEquals(after.depth, 0)
    assertEquals(after.focus.children.length, 1)

  test("upN moves up multiple levels"):
    val c0 = Cursor.root
    val c1 = c0.addChild(Node.Element[NormalType, tags.gen.HTMLDivElement]("div"))
    val c2 = c1.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val c3 = c2.addChild(Node.Element[NormalType, tags.gen.HTMLAnchorElement]("a"))
    assertEquals(c3.depth, 3)
    val back = c3.upN(2)
    assertEquals(back.depth, 1)
