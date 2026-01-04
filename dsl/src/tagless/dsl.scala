package tagless

import tags.gen.{Element => Elem}
import tags.gen.HtmlTag
import tags.setters.{
  Setter,
  HtmlAttrSetter,
  PropSetter,
  TailwindAttrSetter,
  AriaAttrSetter
}
import dom.{
  Node,
  AttrNode,
  Dom,
  Cursor,
  Tree,
  NormalType,
  VoidType,
  NodeType,
  NotRoot
}

object dsl:

  export md.MarkdownConverterExtensions.*
  export md.MarkdownConverterExtensions.given
  export dom.CursorExtensions.*
  export dom.CursorExtensions.given

  import Cursor.*

  final case class CursorAssembly[N <: NodeType, E <: tags.gen.Element](
      cursor: Cursor[N, E],
      nrOfReplications: Int
  )

  def root: Cursor[NormalType, tags.gen.HTMLBaseElement] = Cursor.root

  extension [E <: tags.gen.Element, N <: tags.gen.NodeType](
      tag: tags.gen.HtmlTag[E, N]
  )
    def unary_~ : Cursor[ToHtmlNodeType[N], E] =
      val node =
        if tag.void then Node.VoidElement(tag.domName)
        else Node.Element(tag.domName)
      Cursor(node)

  extension [N <: NodeType, E <: tags.gen.Element](
      node: Node[N, E]
  ) {
    def unary_~ : Cursor[N, E] =
      Cursor(node)

    def +:(siblings: Iterable[Node[N, E]]): Vector[Node[N, E]] =
      node +: siblings.toVector
  }

  extension [E <: tags.gen.Element, N <: NormalType](
      c: Cursor[NormalType, E]
  ) {
    def >>[N1 <: NodeType, E1 <: tags.gen.Element](
        node: Node[N1, E1]
    ): Cursor[N1, E1] =
      c.addChildAndEnter[N1, E1](Tree(node, Vector.empty))

    def >>(rep: Repeated): Cursor[NormalType, E] =
      c.addChildrenStay(List.fill(rep.times)(rep.fragment.root))

    /** Adding a fragment as child(ren) but staying at the current cursor
      * position. This is very convenient when you have built a standalone html
      * snippet (cursor). It is implicitly converted into a Fragment when passed
      * to this method.
      *
      * @param fragment
      * @return
      */
    def >>^(fragment: Fragment): Cursor[NormalType, E] =
      c.addChildrenStay(List(fragment.root))

    def >>[A](xs: IterableOnce[A])(using
        conv: ToTrees[A]
    ): Cursor[NormalType, E] =
      c.addChildrenStay(conv(xs.iterator.to(List)))

    def >>^(m: md.Markdown): Cursor[NormalType, E] =
      md.MarkdownConverter.build(m, c)
  }

  extension [N <: NodeType, E <: tags.gen.Element](c: Cursor[N, E]) {

    def >^[N1 <: NodeType, E1 <: tags.gen.Element](
        node: Node[N1, E1]
    )(using
        ev: NotRoot[E]
    ): Cursor[N1, E1] =
      c.addSiblingRightStay[N1, E1](List(node))

    def >^[N1 <: NodeType, E1 <: tags.gen.Element](
        siblings: List[Node[N1, E1]]
    )(using
        ev: NotRoot[E]
    ): Cursor[N1, E1] =
      c.addSiblingRightStay[N1, E1](siblings)

    def >[N1 <: NodeType, E1 <: tags.gen.Element](
        node: Node[N1, E1]
    )(using
        ev: NotRoot[E]
    ): Cursor[N1, E1] =
      c.addSiblingRightAndEnter[N1, E1](Vector(node))

    def >[N1 <: NodeType, E1 <: tags.gen.Element](
        siblings: Vector[Node[N1, E1]]
    )(using
        ev: NotRoot[E]
    ): Cursor[N1, E1] =
      c.addSiblingRightAndEnter[N1, E1](siblings)

    /** Converting two standalone html snippets (cursors) into a list of
      * Trees, this is useful when you need to these as siblings in one
      * step, to the current cursor position.
      *
      * @param N1
      * @param E1
      * @param siblingCursor
      * @param ev
      * @return
      */
    def +[N1 <: NodeType, E1 <: tags.gen.Element](
        siblingCursor: Cursor[N1, E1]
    )(using
        ev: NotRoot[E]
    ): List[Tree[Dom]] =
      List(c.seal, siblingCursor.seal)

    def <^(i: Int): Cursor[NormalType, tags.gen.Element] =
      c.upN(i)

    def ^ : Cursor[NormalType, tags.gen.Element] =
      c.upN(1)

    def *(i: Int): Repeated = Repeated(Fragment(c.seal), i)

  }

  given [A <: Elem, N <: tags.gen.NodeType]
      : Conversion[List[HtmlTag[A, N]], List[Dom]] with
    def apply(htmlTags: List[tags.gen.HtmlTag[A, N]]): List[Dom] =
      htmlTags.map: htmlTag =>
        if htmlTag.void then Node.VoidElement(htmlTag.domName)
        else Node.Element(htmlTag.domName)

  given [A <: tags.gen.SVGElement]: Conversion[tags.gen.SvgTag[A], Dom] with
    def apply(svgTag: tags.gen.SvgTag[A]): Dom = Node.Element(svgTag.domName)

  given [A <: Elem]: Conversion[Iterable[A], List[A]] with
    def apply(value: Iterable[A]): List[A] = value.toList

  given [N <: NodeType, E <: tags.gen.Element]
      : Conversion[Cursor[N, E], Fragment] with
    def apply(cursor: Cursor[N, E]): Fragment =
      Fragment(cursor.seal)
