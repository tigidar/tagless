package md

// Minimal generic rose tree
enum Tree[+A]:
  case Node(value: A, children: Vector[Tree[A]])

object Tree:
  def leaf[A](a: A): Tree[A] = Node(a, Vector.empty)

  /** Start building at a fresh root. */
  def builder[A](root: A): Cursor[A] =
    Cursor(Node(root, Vector.empty), List.empty)

/** Breadcrumb for the zipper, siblings to the left/right + parent value */
final case class Ctx[+A](
    left: Vector[Tree[A]],
    parent: A,
    right: Vector[Tree[A]]
)

/** Zipper focus for incremental building (adds only). */
final case class Cursor[+A](focus: Tree.Node[A], stack: List[Ctx[A]]):
  self =>
  import Tree.*

  def depth: Int = stack.length

  def isRoot: Boolean = stack.isEmpty

  def parent: Option[A] = stack.headOption.map(_.parent)

  // --- internal helper to keep parent children updated after addChild
  private def withParentChildren[B >: A](
      newChildren: Vector[Tree[B]]
  ): Cursor[B] = stack match
    case Ctx(ls, p, rs) :: up =>
      val rebuiltParent =
        Tree.Node(p, ls ++ (focus +: rs)).copy(children = newChildren)
      Cursor(
        focus,
        Ctx(ls, p, rs) :: up
      )
    case Nil => self

  /** Add a child as the new RIGHTMOST child and move focus to it. */
  def addChild[B >: A](a: B): Cursor[B] =
    val child: Node[B] = Node(a, Vector.empty)
    val updated = focus.copy(children = focus.children :+ child)
    Cursor(child, Ctx(focus.children, focus.value, Vector.empty) :: stack)
      .copy(focus = child)
      .withParentChildren(updated.children)

  /** Step up one level (if any). */
  def up: Option[Cursor[A]] = stack match
    case Ctx(ls, p, rs) :: up =>
      val rebuilt: Node[A] = Node(p, ls ++ (focus +: rs))
      Some(Cursor(rebuilt, up))
    case Nil => None

  /** Step up n levels (clamped at root). */
  def up(n: Int): Cursor[A] =
    Iterator
      .iterate(Option(self))(_.flatMap(_.up))
      .drop(n)
      .next()
      .getOrElse(self)

  /** Extract the fully rebuilt tree. */
  def result: Tree[A] =
    up.fold[Tree[A]](focus)(_.result)

extension [A](stk: List[Ctx[A]])
  private def copyHeadParent[B >: A](rebuilt: Tree.Node[B]): List[Ctx[B]] =
    stk match
      case Ctx(ls, _, rs) :: tail =>
        Ctx(ls, rebuilt.value, rs) :: tail
      case Nil => Nil
/*
  /** Add a right sibling and move focus to it. */
  def addRightSibling[B >: A](a: B): Cursor[B] = stack match
    case Ctx(ls, p, rs) :: up =>
      val sib = Node(a, Vector.empty)
      Cursor(sib, Ctx(ls :+ focus, p, rs) :: up)
    case Nil =>
      // no parent -> cannot add a sibling at root
      this.asInstanceOf[Cursor[B]]

  /** Add a left sibling and move focus to it. */
  def addLeftSibling[B >: A](a: B): Cursor[B] = stack match
    case Ctx(ls, p, rs) :: up =>
      val sib = Node(a, Vector.empty)
      Cursor(sib, Ctx(ls, p, focus +: rs) :: up)
    case Nil =>
      this.asInstanceOf[Cursor[B]]

  /** Move to the first/last child if present. */
  def firstChild: Option[Cursor[A]] =
    focus.children.headOption.map { case n: Node[A] =>
      Cursor(n, Ctx(Vector.empty, focus.value, focus.children.tail) :: stack)
    }

  def lastChild: Option[Cursor[A]] =
    focus.children.lastOption.map { case n: Node[A] =>
      Cursor(n, Ctx(focus.children.init, focus.value, Vector.empty) :: stack)
    }

  /** Move to a left/right sibling if present. */
  def left: Option[Cursor[A]] = stack match
    case Ctx(ls, p, rs) :: up if ls.nonEmpty =>
      val n = ls.last.asInstanceOf[Node[A]]
      Some(Cursor(n, Ctx(ls.init, p, focus +: rs) :: up))
    case _ => None

  def right: Option[Cursor[A]] = stack match
    case Ctx(ls, p, rs) :: up if rs.nonEmpty =>
      val n = rs.head.asInstanceOf[Node[A]]
      Some(Cursor(n, Ctx(ls :+ focus, p, rs.tail) :: up))
    case _ => None

  /** Step up one level (if any). */
  def up: Option[Cursor[A]] = stack match
    case Ctx(ls, p, rs) :: up =>
      val rebuilt = Node(p, ls ++ (focus +: rs))
      Some(Cursor(rebuilt, up))
    case Nil => None

  /** Step up n levels (clamped at root). */
  def up(n: Int): Cursor[A] =
    Iterator
      .iterate(Option(this))(_.flatMap(_.up))
      .drop(n)
      .next()
      .getOrElse(this)

  /** Extract the fully rebuilt tree. */
  def result: Tree[A] =
    up.fold[Tree[A]](focus)(_.result)

  // --- internal helper to keep parent children updated after addChild
  private def withParentChildren[B >: A](
      newChildren: Vector[Tree[B]]
  ): Cursor[B] = stack match
    case Ctx(ls, p, rs) :: up =>
      val rebuiltParent =
        Tree.Node(p, ls ++ (focus +: rs)).copy(children = newChildren)
      Cursor(
        focus.asInstanceOf[Tree.Node[B]],
        Ctx(ls, p, rs) :: up.copyHeadParent(rebuiltParent)
      )
    case Nil => this.asInstanceOf[Cursor[B]]

  extension (stk: List[Ctx[?]])
    private def copyHeadParent[B](rebuilt: Tree.Node[B]): List[Ctx[B]] =
      stk match
        case Ctx(ls, _, rs) :: tail =>
          Ctx(ls, rebuilt.value, rs) :: tail.asInstanceOf[List[Ctx[B]]]
        case Nil => Nil
 */
