package tagless

import dom.{Node, AttrNode, Dom, Cursor, Tree, NormalType, VoidType, NodeType}

final case class Fragment(root: Tree[Dom])
final case class Repeated(fragment: Fragment, times: Int)

trait ToTrees[A]:
  def apply(xs: List[A]): List[Tree[Dom]]

object ToTrees:
  given ToTrees[Fragment] with
    def apply(xs: List[Fragment]) = xs.map(_.root)

  given ToTrees[Dom] with
    def apply(xs: List[Dom]) = xs.map(d => Tree[Dom](d, Vector.empty))

  given identity: ToTrees[Tree[Dom]] with
    inline def apply(xs: List[Tree[Dom]]) = xs

  given forCursor[N <: NodeType, E <: tags.gen.Element]: ToTrees[Cursor[N, E]]
  with
    def apply(xs: List[Cursor[N, E]]) = xs.map(_.seal)
