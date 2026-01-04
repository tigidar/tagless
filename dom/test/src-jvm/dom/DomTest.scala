package dom

import munit.*
import tags.T.*

/** Helper to extract tag from a Dom node */
def getTag(dom: Dom): String = dom match
  case Node.Element(tag, _)     => tag
  case Node.VoidElement(tag, _) => tag
  case Node.Text(_)             => "<text>"

class CtxTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("Ctx holds left siblings, parent, and right siblings"):
    val parent = divNode
    val leftSibling = Tree[Dom](Node.Element("span"), Vector.empty)
    val rightSibling = Tree[Dom](Node.Element("p"), Vector.empty)

    val ctx = Ctx(
      left = Vector(leftSibling),
      parent = parent,
      right = Vector(rightSibling)
    )

    assertEquals(ctx.left.length, 1)
    assertEquals(ctx.right.length, 1)
    assertEquals(ctx.parent, parent)

  test("Ctx can have empty left and right siblings"):
    val parent = divNode
    val ctx = Ctx(
      left = Vector.empty,
      parent = parent,
      right = Vector.empty
    )

    assert(ctx.left.isEmpty)
    assert(ctx.right.isEmpty)

  test("Ctx preserves order of siblings"):
    val parent = divNode
    val left1 = Tree[Dom](Node.Element("span"), Vector.empty)
    val left2 = Tree[Dom](Node.Element("p"), Vector.empty)
    val right1 = Tree[Dom](Node.Element("a"), Vector.empty)
    val right2 = Tree[Dom](Node.Element("b"), Vector.empty)

    val ctx = Ctx(
      left = Vector(left1, left2),
      parent = parent,
      right = Vector(right1, right2)
    )

    assertEquals(getTag(ctx.left(0).value), "span")
    assertEquals(getTag(ctx.left(1).value), "p")
    assertEquals(getTag(ctx.right(0).value), "a")
    assertEquals(getTag(ctx.right(1).value), "b")

class CursorBasicsTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("Cursor.apply creates cursor at root with empty stack"):
    val cursor = Cursor(divNode)

    assert(cursor.isRoot)
    assertEquals(cursor.depth, 0)
    assertEquals(cursor.stack, List.empty)

  test("Cursor.root creates an html root cursor"):
    val cursor = Cursor.root

    assert(cursor.isRoot)
    assertEquals(cursor.depth, 0)
    assertEquals(getTag(cursor.focus.value), "html")

  test("depth returns stack length"):
    val root = Cursor(divNode)
    assertEquals(root.depth, 0)

    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    assertEquals(child.depth, 1)

    val grandchild = child.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )
    assertEquals(grandchild.depth, 2)

  test("isRoot returns true only for root cursor"):
    val root = Cursor(divNode)
    assert(root.isRoot)

    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    assert(!child.isRoot)

  test("parent returns None for root cursor"):
    val root = Cursor(divNode)
    assertEquals(root.parent, None)

  test("parent returns Some parent node for non-root cursor"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    assert(child.parent.isDefined)
    assertEquals(getTag(child.parent.get), "div")

  test("debug returns readable cursor info"):
    val cursor = Cursor(divNode)
    val debug = cursor.debug

    assert(debug.contains("depth=0"))
    assert(debug.contains("Cursor"))

class CursorNavigationTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("up navigates to parent cursor"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val back = child.up

    assert(back.isRoot)
    assertEquals(getTag(back.focus.value), "div")

  test("up throws AssertionError when called on root"):
    val root = Cursor(divNode)

    intercept[AssertionError] {
      root.up
    }

  test("up reconstructs parent with child included"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val back = child.up

    assertEquals(back.focus.children.length, 1)
    assertEquals(getTag(back.focus.children.head.value), "span")

  test("upN navigates multiple levels"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val grandchild = child.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )

    val backToRoot = grandchild.upN(2)

    assert(backToRoot.isRoot)

  test("upN with 0 returns same cursor"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val same = child.upN(0)

    assertEquals(same.depth, child.depth)

  test("upN with 1 is equivalent to up"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val viaUpN = child.upN(1)
    val viaUp = child.up

    assertEquals(viaUpN.focus.value, viaUp.focus.value)
    assertEquals(viaUpN.depth, viaUp.depth)

class CursorChildOperationsTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("addChild creates child and descends into it"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    assertEquals(child.depth, 1)
    assertEquals(getTag(child.focus.value), "span")

  test("addChild puts previous children to the left in context"):
    val root = Cursor(divNode)
    val firstChild =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val backUp = firstChild.up
    val secondChild = backUp.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )

    assertEquals(secondChild.stack.head.left.length, 1)
    assertEquals(getTag(secondChild.stack.head.left.head.value), "span")

  test("addChildStay with Node adds child but keeps focus at parent"):
    val root = Cursor(divNode)
    val sameLevel = root.addChildStay(
      Node.Element[NormalType, tags.gen.HTMLSpanElement]("span")
    )

    assertEquals(sameLevel.depth, 0)
    assertEquals(sameLevel.focus.children.length, 1)
    assertEquals(getTag(sameLevel.focus.children.head.value), "span")

  test("addChildStay with Tree adds child but keeps focus at parent"):
    val root = Cursor(divNode)
    val childTree = Tree[Dom](Node.Element("span"), Vector.empty)
    val sameLevel = root.addChildStay(childTree)

    assertEquals(sameLevel.depth, 0)
    assertEquals(sameLevel.focus.children.length, 1)

  test("addChildrenStay adds multiple children"):
    val root = Cursor(divNode)
    val children: List[Tree[Dom]] = List(
      Tree[Dom](Node.Element("span"), Vector.empty),
      Tree[Dom](Node.Element("p"), Vector.empty),
      Tree[Dom](Node.Element("a"), Vector.empty)
    )

    val result = root.addChildrenStay(children)

    assertEquals(result.focus.children.length, 3)

  test("addChildAndEnter with Tree adds child and descends"):
    val root = Cursor(divNode)
    val childTree = Tree[Dom](Node.Element("span"), Vector.empty)
    val child =
      root.addChildAndEnter[NormalType, tags.gen.HTMLSpanElement](childTree)

    assertEquals(child.depth, 1)
    assertEquals(getTag(child.focus.value), "span")

  test("nested addChild creates proper tree structure"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val grandchild = child.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )

    assertEquals(grandchild.depth, 2)

    val result = grandchild.resultTree
    assertEquals(result.children.length, 1)
    assertEquals(result.children.head.children.length, 1)

class CursorSiblingOperationsTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("addSiblingRightStay with list adds siblings to right"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val withSiblings = child.addSiblingRightStay(
      List(
        Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
      )
    )

    assertEquals(withSiblings.stack.head.right.length, 1)

  test("addSiblingRightStay with Tree adds sibling to right"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val siblingTree = Tree[Dom](Node.Element("p"), Vector.empty)

    val withSibling =
      child.addSiblingRightStay[NormalType, tags.gen.HTMLParagraphElement](
        siblingTree
      )

    assertEquals(withSibling.stack.head.right.length, 1)

  test("addSiblingRightStay keeps focus on original node"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val withSiblings = child.addSiblingRightStay(
      List(
        Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
      )
    )

    assertEquals(getTag(withSiblings.focus.value), "span")

  test("addSiblingRightAndEnter adds siblings and moves focus"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val moved =
      child.addSiblingRightAndEnter[NormalType, tags.gen.HTMLParagraphElement](
        Vector(Node.Element("p"))
      )

    assertEquals(getTag(moved.focus.value), "p")

  test("addSiblingRightAndEnter puts original focus to left"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val moved =
      child.addSiblingRightAndEnter[NormalType, tags.gen.HTMLParagraphElement](
        Vector(Node.Element("p"))
      )

    assert(moved.stack.head.left.nonEmpty)
    assertEquals(getTag(moved.stack.head.left.last.value), "span")

class CursorSealAndResultTreeTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("seal rebuilds tree from focus to root"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    val sealedTree = child.seal

    assertEquals(getTag(sealedTree.value), "div")
    assertEquals(sealedTree.children.length, 1)

  test("seal at root returns just the focus"):
    val root = Cursor(divNode)

    val sealedTree = root.seal

    assertEquals(getTag(sealedTree.value), "div")

  test("seal preserves siblings"):
    val root = Cursor(divNode)
    val child1 =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val backUp = child1.up
    val child2 = backUp.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )

    val sealedTree = child2.seal

    assertEquals(sealedTree.children.length, 2)

  test("resultTree produces complete tree from any cursor position"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val grandchild = child.addChild(
      Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
    )

    val tree = grandchild.resultTree

    assertEquals(getTag(tree.value), "div")

  test("resultTree includes all siblings in correct order"):
    val root = Cursor(divNode)
    val child1 =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))
    val withSibling = child1.addSiblingRightStay(
      List(
        Node.Element[NormalType, tags.gen.HTMLParagraphElement]("p")
      )
    )

    val tree = withSibling.resultTree

    assertEquals(tree.children.length, 2)
    assertEquals(getTag(tree.children(0).value), "span")
    assertEquals(getTag(tree.children(1).value), "p")

class CursorEdgeCasesTest extends FunSuite:

  def divNode: Node[NormalType, tags.gen.HTMLDivElement] =
    Node.Element("div")

  test("VoidElement can be added as child"):
    val root = Cursor(divNode)
    val voidChild = root.addChildStay(
      Node.VoidElement[VoidType, tags.gen.HTMLInputElement]("input")
    )

    assertEquals(voidChild.focus.children.length, 1)
    val childNode = voidChild.focus.children.head.value
    assert(childNode.isInstanceOf[Node.VoidElement[?, ?]])

  test("Text node can be added as child"):
    val root = Cursor(divNode)
    val textTree = Tree[Dom](Node.Text("Hello World"), Vector.empty)
    val textChild = root.addChildStay(textTree)

    assertEquals(textChild.focus.children.length, 1)
    val childNode = textChild.focus.children.head.value
    childNode match
      case Node.Text(text) => assertEquals(text, "Hello World")
      case other           => fail(s"Expected Node.Text but got $other")

  test("Element with attributes is preserved"):
    val elemWithAttrs = Node.Element[NormalType, tags.gen.HTMLDivElement](
      "div",
      List(AttrNode.HtmlAttr("class", "container"))
    )
    val root = Cursor(elemWithAttrs)

    root.focus.value match
      case Node.Element(_, attrs) => assertEquals(attrs.length, 1)
      case other                  => fail(s"Expected Node.Element but got $other")

    val tree = root.resultTree
    tree.value match
      case Node.Element(_, attrs) => assertEquals(attrs.length, 1)
      case other                  => fail(s"Expected Node.Element but got $other")

  test("Deep nesting works correctly"):
    var cursor: Cursor[NormalType, ?] = Cursor(divNode)

    for i <- 1 to 10 do
      cursor = cursor.addChild(
        Node.Element[NormalType, tags.gen.HTMLSpanElement](s"level-$i")
      )

    assertEquals(cursor.depth, 10)

    val tree = cursor.resultTree
    var current = tree
    for i <- 1 to 10 do
      assertEquals(current.children.length, 1)
      current = current.children.head

  test("Multiple children at same level"):
    val root = Cursor(divNode)
    val spanTree = Tree[Dom](Node.Element("span"), Vector.empty)
    val pTree = Tree[Dom](Node.Element("p"), Vector.empty)
    val aTree = Tree[Dom](Node.Element("a"), Vector.empty)
    val result = root
      .addChildStay(spanTree)
      .addChildStay(pTree)
      .addChildStay(aTree)

    assertEquals(result.focus.children.length, 3)

  test("addSiblingRightAndEnter requires non-empty siblings"):
    val root = Cursor(divNode)
    val child =
      root.addChild(Node.Element[NormalType, tags.gen.HTMLSpanElement]("span"))

    intercept[IllegalArgumentException] {
      child.addSiblingRightAndEnter[NormalType, tags.gen.HTMLParagraphElement](
        Vector.empty
      )
    }
