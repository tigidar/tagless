package md

import munit.*

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
