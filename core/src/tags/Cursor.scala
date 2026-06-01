package tags

/** Context for a position in the tree - breadcrumb for navigation.
  *
  * When we descend into a child, we push a Context onto the stack recording the
  * parent node and any siblings. This allows us to reconstruct the tree when we
  * ascend back up.
  */
final case class Context(
    left: Vector[Node], // Siblings to the left of where we descended
    parent: Node.Element // The parent element we descended from
)

/** Cursor for navigating and building the HTML tree.
  *
  * Type parameters:
  *   - D: Depth phantom type (D0, Succ[D0], etc.) - tracks how deep we are
  *   - K: ElementKind phantom type (Normal or Void) - tracks current element
  *     type
  *
  * The focus is the current node we're building. The stack contains Context
  * records for all ancestors, allowing us to rebuild the tree when done.
  */
final case class Cursor[D <: Depth, K <: ElementKind](
    focus: Node,
    stack: List[Context]
):
  /** Seal the cursor - close all open elements and return the root */
  def seal: Node =
    stack.foldLeft(focus) { (child, ctx) =>
      Node.addChild(ctx.parent, child)
    }

object Cursor:
  /** Create a cursor at root level from a normal element */
  def apply(node: Node.Element): Cursor[D0, Normal] =
    Cursor[D0, Normal](node, Nil)

  /** Create a cursor at root level from a void element */
  def applyVoid(node: Node.VoidElement): Cursor[D0, Void] =
    Cursor[D0, Void](node, Nil)

  /** Internal: add child and descend - used by >> operator */
  def addChildAndDescend[D <: Depth, K2 <: ElementKind](
      cursor: Cursor[D, Normal],
      childNode: Node
  ): Cursor[Succ[D], K2] =
    cursor.focus match
      case parent: Node.Element =>
        val ctx = Context(parent.children, parent.copy(children = Vector.empty))
        Cursor[Succ[D], K2](childNode, ctx :: cursor.stack)
      case _ =>
        // This should not happen if types are correct
        throw new IllegalStateException("Cannot descend from non-element node")

  /** Internal: add child and stay - used by >>^ operator */
  def addChildAndStay[D <: Depth](
      cursor: Cursor[D, Normal],
      childNode: Node
  ): Cursor[D, Normal] =
    cursor.focus match
      case parent: Node.Element =>
        val newParent = Node.addChild(parent, childNode)
        cursor.copy(focus = newParent)
      case _ =>
        throw new IllegalStateException("Cannot add child to non-element node")

  /** Internal: add sibling and move to it - used by > operator */
  def addSiblingAndMove[D <: Depth, K <: ElementKind, K2 <: ElementKind](
      cursor: Cursor[Succ[D], K],
      siblingNode: Node
  ): Cursor[Succ[D], K2] =
    cursor.stack match
      case ctx :: rest =>
        val newLeft = ctx.left :+ cursor.focus
        val newCtx = ctx.copy(left = newLeft)
        Cursor[Succ[D], K2](siblingNode, newCtx :: rest)
      case Nil =>
        throw new IllegalStateException("Cannot add sibling at root level")

  /** Internal: add sibling and stay - used by >^ operator */
  def addSiblingAndStay[D <: Depth, K <: ElementKind](
      cursor: Cursor[Succ[D], K],
      siblingNode: Node
  ): Cursor[Succ[D], K] =
    cursor.stack match
      case ctx :: rest =>
        val newLeft = ctx.left :+ siblingNode
        val newCtx = ctx.copy(left = newLeft)
        cursor.copy(stack = newCtx :: rest)
      case Nil =>
        throw new IllegalStateException("Cannot add sibling at root level")

  /** Internal: ascend one level - used by ^ operator */
  def ascend[D <: Depth, K <: ElementKind](
      cursor: Cursor[Succ[D], K]
  ): Cursor[D, Normal] =
    cursor.stack match
      case ctx :: rest =>
        // Rebuild parent with all children (left siblings + current focus)
        val allChildren = ctx.left :+ cursor.focus
        val rebuiltParent = ctx.parent.copy(children = allChildren)
        Cursor[D, Normal](rebuiltParent, rest)
      case Nil =>
        throw new IllegalStateException("Cannot ascend past root")
