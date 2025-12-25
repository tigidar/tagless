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
      case Ctx(ls, _, rs) :: tail => Ctx(ls, rebuilt.value, rs) :: tail
      case Nil                    => Nil
