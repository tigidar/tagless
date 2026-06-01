package tags

/** Node ADT representing the HTML tree structure.
  *
  * This is the data structure that gets built by the DSL and then rendered to
  * HTML string or DOM elements.
  */
enum Node:
  /** Normal element with tag name, attributes, and children */
  case Element(tag: String, attrs: List[Attr], children: Vector[Node])

  /** Void/self-closing element with tag name and attributes, no children */
  case VoidElement(tag: String, attrs: List[Attr])

  /** Text content node */
  case Text(content: String)

  /** Fragment containing multiple sibling nodes */
  case Fragment(nodes: Vector[Node])

object Node:
  /** Create an empty element */
  def element(tag: String): Node.Element =
    Element(tag, Nil, Vector.empty)

  /** Create an element with attributes */
  def element(tag: String, attrs: List[Attr]): Node.Element =
    Element(tag, attrs, Vector.empty)

  /** Create a void element */
  def voidElement(tag: String): Node.VoidElement =
    VoidElement(tag, Nil)

  /** Create a void element with attributes */
  def voidElement(tag: String, attrs: List[Attr]): Node.VoidElement =
    VoidElement(tag, attrs)

  /** Create a text node */
  def text(content: String): Node.Text =
    Text(content)

  /** Create a fragment from nodes */
  def fragment(nodes: Node*): Node.Fragment =
    Fragment(nodes.toVector)

  /** Add a child to an element */
  def addChild(parent: Node.Element, child: Node): Node.Element =
    parent.copy(children = parent.children :+ child)

  /** Add children to an element */
  def addChildren(parent: Node.Element, children: Vector[Node]): Node.Element =
    parent.copy(children = parent.children ++ children)
