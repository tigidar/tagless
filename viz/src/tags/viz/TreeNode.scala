package tags.viz

import tags.{Attr, Node}

/** Node type classification for visualization */
enum NodeType derives CanEqual:
  case Element
  case Void
  case Text
  case Fragment
  case Component

/** Metadata about a component boundary */
final case class ComponentMeta(
    name: String,
    category: String
)

/** Statistics about a tree or subtree */
final case class TreeStats(
    totalNodes: Int,
    maxDepth: Int,
    elementCount: Int,
    voidCount: Int,
    textCount: Int,
    componentCount: Int,
    elementsByTag: Map[String, Int],
    idsFound: Set[String]
):
  def +(other: TreeStats): TreeStats = TreeStats(
    totalNodes = totalNodes + other.totalNodes,
    maxDepth = math.max(maxDepth, other.maxDepth),
    elementCount = elementCount + other.elementCount,
    voidCount = voidCount + other.voidCount,
    textCount = textCount + other.textCount,
    componentCount = componentCount + other.componentCount,
    elementsByTag =
      other.elementsByTag.foldLeft(elementsByTag) { case (acc, (tag, count)) =>
        acc.updated(tag, acc.getOrElse(tag, 0) + count)
      },
    idsFound = idsFound ++ other.idsFound
  )

object TreeStats:
  val empty: TreeStats = TreeStats(
    totalNodes = 0,
    maxDepth = 0,
    elementCount = 0,
    voidCount = 0,
    textCount = 0,
    componentCount = 0,
    elementsByTag = Map.empty,
    idsFound = Set.empty
  )

  def forElement(tag: String, id: Option[String], depth: Int): TreeStats =
    TreeStats(
      totalNodes = 1,
      maxDepth = depth,
      elementCount = 1,
      voidCount = 0,
      textCount = 0,
      componentCount = 0,
      elementsByTag = Map(tag -> 1),
      idsFound = id.toSet
    )

  def forVoid(tag: String, id: Option[String], depth: Int): TreeStats =
    TreeStats(
      totalNodes = 1,
      maxDepth = depth,
      elementCount = 0,
      voidCount = 1,
      textCount = 0,
      componentCount = 0,
      elementsByTag = Map(tag -> 1),
      idsFound = id.toSet
    )

  def forText(depth: Int): TreeStats = TreeStats(
    totalNodes = 1,
    maxDepth = depth,
    elementCount = 0,
    voidCount = 0,
    textCount = 1,
    componentCount = 0,
    elementsByTag = Map.empty,
    idsFound = Set.empty
  )

/** A visualization node - the tree structure for rendering */
final case class TreeNode(
    id: String,
    tag: String,
    nodeType: NodeType,
    attributes: Map[String, String],
    depth: Int,
    siblingIndex: Int,
    childCount: Int,
    textContent: Option[String],
    component: Option[ComponentMeta],
    children: Vector[TreeNode]
):
  /** Display label for visualization (e.g., "div#my-id.my-class") */
  def label: String =
    val idPart = attributes.get("id").map("#" + _).getOrElse("")
    val classPart =
      attributes.get("class").map("." + _.replace(" ", ".")).getOrElse("")
    val componentPart = component.map(c => s" @${c.name}").getOrElse("")
    s"$tag$idPart$classPart$componentPart"

  /** Short label (tag + id only) */
  def shortLabel: String =
    val idPart = attributes.get("id").map("#" + _).getOrElse("")
    s"$tag$idPart"

/** Complete tree visualization with metadata */
final case class TreeVisualization(
    root: TreeNode,
    stats: TreeStats
):
  /** Convert to D3-compatible JSON */
  def toD3Json: String =
    D3Serializer.serialize(root)

  /** Convert to ASCII tree representation */
  def toAsciiTree: String =
    AsciiTreeRenderer.render(root)

  /** Convert to Mermaid diagram syntax */
  def toMermaid: String =
    MermaidRenderer.render(root)
