package dom

import munit.*

class TreeTest extends FunSuite:

  test("simple element to HTML"):
    val tree: Tree[Dom] = Tree.Node(
      Node.Element("div", List(AttrNode.HtmlAttr("id", "main"))),
      Vector.empty
    )
    val html = tree.toHtml
    assertEquals(html, """<div id="main" ></div>
""")

  test("void element to HTML"):
    val tree: Tree[Dom] = Tree.Node(
      Node.VoidElement("br", Nil),
      Vector.empty
    )
    val html = tree.toHtml
    assertEquals(html, "<br />")

  test("element with text content"):
    val tree: Tree[Dom] = Tree.Node(
      Node.Element("p", List(AttrNode.Text("Hello"))),
      Vector.empty
    )
    val html = tree.toHtml
    assertEquals(html, """<p >Hello</p>
""")

  test("nested elements"):
    val child = Tree.Node[Dom](
      Node.Element("span", List(AttrNode.Text("inner"))),
      Vector.empty
    )
    val parent: Tree[Dom] = Tree.Node(
      Node.Element("div", Nil),
      Vector(child)
    )
    val html = parent.toHtml
    assertEquals(html, """<div ><span >inner</span>
</div>
""")

  test("text node renders plain text"):
    val tree: Tree[Dom] = Tree.Node(
      Node.Text("plain text"),
      Vector.empty
    )
    val html = tree.toHtml
    assertEquals(html, "plain text")
