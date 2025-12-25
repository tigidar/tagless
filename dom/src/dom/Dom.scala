package dom

import scala.annotation.implicitNotFound

@implicitNotFound(
  "Cannot add sibling to the right of the html root node (it has no parent)."
)
sealed trait NotRoot[E <: tags.gen.Element]
object NotRoot:
  given [E <: tags.gen.Element](using
      scala.util.NotGiven[E =:= tags.gen.HTMLBaseElement]
  ): NotRoot[E] = new NotRoot[E] {}

sealed trait NodeType
sealed trait NormalType extends NodeType
sealed trait VoidType extends NodeType
sealed trait TextType extends NodeType

type Dom = Node[NodeType, tags.gen.Element]

enum Node[+N <: NodeType, +E <: tags.gen.Element] derives CanEqual:
  case Element[N <: NodeType, E <: tags.gen.Element](
      tag: String,
      attrs: List[AttrNode] = Nil
  ) extends Node[N, E]

  case VoidElement[N <: NodeType, E <: tags.gen.Element](
      tag: String,
      attrs: List[AttrNode] = Nil
  ) extends Node[N, E]

  case Text(value: String) extends Node[TextType, Nothing]

/** Breadcrumb for the zipper, siblings to the left/right + parent value This is
  * the pointer into the tree structure, it's the place where we are sitting.
  *
  * It is important to note that this does not contain the focus node itself,
  * but only its siblings and parent. The focus node is stored separately in the
  * Cursor.
  *
  * @param left
  *   \- siblings to the left of the focus node
  * @param parent
  *   \- the parent node of the focus node
  * @param right
  *   \- siblings to the right of the focus node
  */
final case class Ctx(
    left: Vector[Tree[Dom]],
    parent: Node[NormalType, tags.gen.Element],
    right: Vector[Tree[Dom]]
)

// ----------------------------------------
// Type-level guards
// ----------------------------------------
//type RequirePos[D <: Int] = (D > 0) =:= true

/** Zipper focus for incremental building (adds only). A cursor points to a
  * specific node in the tree, and allows navigation and modification of the
  * tree structure. The focus node is the node we are currently at, and the head
  * of the stack contains the context of the focus node (its parent and
  * siblings). Then the rest of the stack contains the contexts of the ancestors
  * of the focus node.
  *
  * Hence, we are able to build the complete tree from the focus node and the
  * stack.
  */
final case class Cursor[N <: NodeType, E <: tags.gen.Element](
    focus: Tree.Node[Dom],
    stack: List[Ctx]
) {

  self =>
  import Node.*

  def depth: Int = stack.length

  def isRoot: Boolean = stack.isEmpty

  def parent: Option[Node[NormalType, tags.gen.Element]] =
    stack.headOption.map(_.parent)

  type IsNormalType[N <: NodeType, E <: tags.gen.Element] <: Node[
    NormalType,
    E
  ] = N match
    case NormalType => Node.Element[NormalType, E]
    case _          => Nothing

  /** Rebuild the closed subtree whose root is the top ancestor in `stack`. */
  def seal: Tree.Node[Dom] =
    stack.foldLeft(focus) { (child, ctx) =>
      Tree.Node[Dom](
        ctx.parent, // parent value
        ctx.left ++ (child +: ctx.right) // left … child … right
      )
    }

  def addChild[
      N1 <: NodeType,
      E1 <: tags.gen.Element
  ](child: Node[N1, E1])(using
      ev: N =:= NormalType
  ): Cursor[N1, E1] =
    val ctx = Ctx(
      left = focus.children.toVector, // already Vector[Tree[Dom]]
      parent = focus.value
        .asInstanceOf[
          Node[dom.NormalType, tags.gen.Element]
        ],
      right = Vector.empty
    )
    val childTree: Tree.Node[Dom] = Tree.Node[Dom](child, Vector.empty)
    Cursor(childTree, ctx :: stack)

  def addChildStay[
      N1 <: NodeType,
      E1 <: tags.gen.Element
  ](child: Node[N1, E1])(using
      ev: N =:= NormalType
  ): Cursor[N, E] =
    val p = focus
    val tree: Tree.Node[Dom] = Tree.Node[Dom](child, Vector.empty)
    Cursor(p.copy(children = p.children :+ tree), stack)
    /*
    val ctx = Ctx(
      left = focus.children.toVector, // already Vector[Tree[Dom]]
      parent = focus.value
        .asInstanceOf[
          Node[html.NormalType, tags.gen.Element]
        ],
      right = Vector.empty
    )
    val childTree: Tree.Node[Dom] = Tree.Node[Dom](child, Vector.empty)
    Cursor(childTree, ctx :: stack)*/

  /** Existing behavior: add a child and move focus into that child. */
  def addChildAndEnter[N1 <: NodeType, E1 <: tags.gen.Element](
      child: Tree.Node[Dom]
  ): Cursor[N1, E1] =
    val parentBefore = focus
    val updatedParent =
      parentBefore.copy(children = parentBefore.children :+ child)
    val newCtx = Ctx(
      left = updatedParent.children.init.toVector, // all but last
      parent =
        updatedParent.value.asInstanceOf[Node[NormalType, tags.gen.Element]],
      right = Vector.empty
    )
    // Focus becomes the child we just appended
    Cursor(focus = child, stack = newCtx :: stack)

  /** New: add a child but keep focus at the parent. */
  def addChildStay(child: Tree.Node[Dom]): Cursor[N, E] =
    val p = focus
    Cursor(p.copy(children = p.children :+ child), stack)

  def addChildrenStay(children: List[Tree.Node[Dom]]): Cursor[N, E] =
    val p = focus
    Cursor(p.copy(children = p.children ++ children), stack)

  import scala.util.NotGiven

  def NotRootElement[E <: tags.gen.Element](using
      scala.util.NotGiven[E =:= tags.gen.HTMLBaseElement]
  ): Unit = ()

  def addSiblingRightStay[
      N1 <: NodeType,
      E1 <: tags.gen.Element
  ](siblings: List[Node[N1, E1]])(using
      ev: NotRoot[E]
  ): Cursor[N1, E1] =
    val trees = siblings.map(s => Tree.Node[Dom](s, Vector.empty))
    stack match
      case Ctx(ls, p, rs) :: up =>
        Cursor(focus, Ctx(ls, p, rs ++ trees) :: up)
      case Nil =>
        println(focus.value)
        println(siblings)
        throw new AssertionError(
          """Unreachable: addSiblingRight called at root node which can not have children""""
        )

  def addSiblingRightStay[
      N1 <: NodeType,
      E1 <: tags.gen.Element
  ](sibling: Tree.Node[Dom])(using
      ev: NotRoot[E]
  ): Cursor[N1, E1] =
    stack match
      case Ctx(ls, p, rs) :: up =>
        Cursor(focus, Ctx(ls, p, rs :+ sibling) :: up)
      case Nil =>
        println(focus.value)
        println(sibling)
        throw new AssertionError(
          "Unreachable: addSiblingRight called at root node which can not have children"
        )

  def addSiblingRightAndEnter[N1 <: NodeType, E1 <: tags.gen.Element](
      siblings: Vector[Node[N1, E1]]
  )(using ev: NotRoot[E]): Cursor[N1, E1] =
    require(siblings.nonEmpty)
    val trees = siblings.dropRight(1).map(s => Tree.Node[Dom](s, Vector.empty))
    stack match
      case Ctx(ls, p, rs) :: up =>
        // focus moves to the *last* appended sibling
        val newFocus: Tree.Node[Dom] = Tree.Node(siblings.head, Vector.empty)

        // everything to the left of the new focus:
        //   existing left  ++ current focus ++ existing right ++ all new siblings except the last
        val newLeft = (ls :+ focus) ++ rs ++ trees

        Cursor(newFocus, Ctx(newLeft, p, Vector.empty) :: up)

      case Nil =>
        throw new AssertionError(
          "Unreachable: addSiblingRight called at root node which cannot have siblings"
        )

  def up: Cursor[NormalType, tags.gen.Element] =
    stack match
      case Ctx(ls, p, rs) :: upTail =>
        val rebuilt: Tree.Node[Dom] =
          Tree.Node[Dom](p, (ls ++ (focus +: rs)))
        Cursor(rebuilt, upTail)
      case Nil =>
        throw new AssertionError(
          s"""Unreachable: up called at root node which has no parent:
          focus=${focus.value}
          Stack is probably empty? $stack"""
        )

  def upN(n: Int): Cursor[NormalType, tags.gen.Element] =
    @annotation.tailrec
    def go(cur: Cursor[?, ?], rem: Int): Cursor[NormalType, tags.gen.Element] =
      if rem <= 0 then cur.asInstanceOf[Cursor[NormalType, tags.gen.Element]]
      else
        cur.up match
          case upCur: Cursor[NormalType, tags.gen.Element] =>
            go(upCur, rem - 1)

    go(this, n)

  def resultTree: Tree[Dom] =
    @annotation.tailrec
    def go(cur: Tree.Node[Dom], stk: List[Ctx]): Tree[Dom] =
      stk match
        case Nil =>
          cur
        case Ctx(ls, p, rs) :: up =>
          // Rebuild the parent by putting the current focus between its left/right siblings
          val rebuilt: Tree.Node[Dom] = Tree.Node[Dom](p, ls ++ (cur +: rs))
          go(rebuilt, up)

    go(focus, stack)

  def debug: String =
    s"""Cursor(depth=$depth, focus=${focus.value}, children=${focus.children.length})"""

}

object CursorExtensions {

  import tags.setters.{
    Setter,
    HtmlAttrSetter,
    PropSetter,
    TailwindAttrSetter,
    AriaAttrSetter
  }

  extension [E <: tags.gen.Element, N <: tags.gen.NodeType](
      t: tags.gen.HtmlTag[E, N]
  ) {

    def *(i: Int): List[Dom] = List.fill(i)(t)

    def apply(
        attrs: (
          Setter[String, String] | HtmlAttrSetter[Iterable[String]] |
            PropSetter[Iterable[String], String] | String
        )*
    ): Node[ToHtmlNodeType[N], E] =
      val attributes = attrs.map {
        case setter: HtmlAttrSetter[?] =>
          AttrNode.HtmlAttr(setter.prop.domName, setter.domValue)
        case setter: PropSetter[?, ?] =>
          AttrNode.Prop(setter.prop.domName, setter.domValue.toString())
        case setter: TailwindAttrSetter[String] =>
          AttrNode.Tailwind(setter.prop.domName, setter.domValue)
        case setter: AriaAttrSetter[String] =>
          AttrNode.Aria(setter.prop.domName, setter.domValue)
        case setter: Setter[?, ?] =>
          AttrNode.Setter(setter.domValue.toString())
        case str: String =>
          AttrNode.Text(str)
      }

      if t.void then Node.VoidElement(t.domName, attributes.toList)
      else Node.Element(t.domName, attributes.toList)
  }

  given [E <: tags.gen.Element, N <: tags.gen.NodeType]
      : Conversion[tags.gen.HtmlTag[E, N], Node[ToHtmlNodeType[N], E]] with
    def apply(htmlTag: tags.gen.HtmlTag[E, N]): Node[ToHtmlNodeType[N], E] =
      if htmlTag.void then Node.VoidElement(htmlTag.domName)
      else Node.Element(htmlTag.domName)

  type ToHtmlNodeType[T <: tags.gen.NodeType] = T match
    case tags.gen.Parent => NormalType
    case tags.gen.Void   => VoidType

}

object Cursor {

  import CursorExtensions.{*, given}

  inline def apply[N <: NodeType, E <: tags.gen.Element](
      root: Node[N, E]
  ): Cursor[N, E] =
    Cursor(Tree.Node(root, Vector.empty), List.empty)

  inline def root: Cursor[NormalType, tags.gen.HTMLBaseElement] =
    Cursor(
      focus = Tree.Node(Node.Element("html"), Vector.empty),
      stack = List.empty
    )

}
