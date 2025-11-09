package tagless

import dom.{Node, AttrNode, Dom, Cursor, Tree, NormalType, VoidType, NodeType}

final case class Fragment(root: Tree.Node[Dom])
final case class Repeated(fragment: Fragment, times: Int)

trait ToTrees[A]:
  def apply(xs: List[A]): List[Tree.Node[Dom]]

object ToTrees:
  given ToTrees[Fragment] with
    def apply(xs: List[Fragment]) = xs.map(_.root)

  given ToTrees[Dom] with
    def apply(xs: List[Dom]) = xs.map(d => Tree.Node[Dom](d, Vector.empty))

  given identity: ToTrees[Tree.Node[Dom]] with
    inline def apply(xs: List[Tree.Node[Dom]]) = xs

  given forCursor[N <: NodeType, E <: tags.gen.Element]: ToTrees[Cursor[N, E]]
  with
    def apply(xs: List[Cursor[N, E]]) = xs.map(_.seal)
