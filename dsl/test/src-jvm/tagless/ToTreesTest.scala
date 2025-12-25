package tagless

import munit.*
import dom.*

class ToTreesTest extends FunSuite:

  test("ToTrees[Fragment] converts fragments to tree nodes"):
    val frag1 = Fragment(Tree.Node[Dom](Node.Element("div"), Vector.empty))
    val frag2 = Fragment(Tree.Node[Dom](Node.Element("span"), Vector.empty))
    val result = summon[ToTrees[Fragment]].apply(List(frag1, frag2))
    assertEquals(result.length, 2)
    assertEquals(result(0).value, Node.Element("div"))
    assertEquals(result(1).value, Node.Element("span"))

  test("ToTrees[Dom] wraps Dom nodes in Tree.Node"):
    val dom1: Dom = Node.Element("p")
    val dom2: Dom = Node.Element("h1")
    val result = summon[ToTrees[Dom]].apply(List(dom1, dom2))
    assertEquals(result.length, 2)
    assertEquals(result(0).value, dom1)
    assertEquals(result(1).value, dom2)

  test("ToTrees[Tree.Node[Dom]] is identity"):
    val node1 = Tree.Node[Dom](Node.Element("div"), Vector.empty)
    val node2 = Tree.Node[Dom](Node.Element("span"), Vector.empty)
    val input = List(node1, node2)
    val result = summon[ToTrees[Tree.Node[Dom]]].apply(input)
    assertEquals(result, input)
