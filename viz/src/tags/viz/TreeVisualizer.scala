package tags.viz

import tags.{Attr, Node}

/** Converts Node trees to visualization-friendly TreeNode structures */
object TreeVisualizer:

  /** Build a complete tree visualization from a Node */
  def build(node: Node): TreeVisualization =
    val (root, stats) =
      buildNode(node, parentPath = "", depth = 0, siblingIdx = 0)
    TreeVisualization(root, stats)

  private def buildNode(
      node: Node,
      parentPath: String,
      depth: Int,
      siblingIdx: Int
  ): (TreeNode, TreeStats) =
    node match
      case Node.Element(tag, attrs, children) =>
        val attrMap = attrsToMap(attrs)
        val id = attrMap.get("id")
        val component = extractComponent(attrs)
        val path = id
          .map(i => s"$parentPath/$i")
          .getOrElse(s"$parentPath/$tag[$siblingIdx]")

        val (childNodes, childStats) = children.zipWithIndex.foldLeft(
          (Vector.empty[TreeNode], TreeStats.empty)
        ) { case ((nodes, stats), (child, idx)) =>
          val (childNode, childStat) = buildNode(child, path, depth + 1, idx)
          (nodes :+ childNode, stats + childStat)
        }

        val nodeStats = TreeStats.forElement(tag, id, depth) + childStats.copy(
          maxDepth =
            if childStats.totalNodes > 0 then childStats.maxDepth else depth,
          componentCount =
            childStats.componentCount + (if component.isDefined then 1 else 0)
        )

        val treeNode = TreeNode(
          id = path,
          tag = tag,
          nodeType =
            if component.isDefined then NodeType.Component
            else NodeType.Element,
          attributes = attrMap,
          depth = depth,
          siblingIndex = siblingIdx,
          childCount = children.size,
          textContent = extractTextContent(children),
          component = component,
          children = childNodes
        )

        (treeNode, nodeStats)

      case Node.VoidElement(tag, attrs) =>
        val attrMap = attrsToMap(attrs)
        val id = attrMap.get("id")
        val path = id
          .map(i => s"$parentPath/$i")
          .getOrElse(s"$parentPath/$tag[$siblingIdx]")

        val treeNode = TreeNode(
          id = path,
          tag = tag,
          nodeType = NodeType.Void,
          attributes = attrMap,
          depth = depth,
          siblingIndex = siblingIdx,
          childCount = 0,
          textContent = None,
          component = None,
          children = Vector.empty
        )

        (treeNode, TreeStats.forVoid(tag, id, depth))

      case Node.Text(content) =>
        val truncated =
          if content.length > 50 then content.take(47) + "..." else content
        val treeNode = TreeNode(
          id = s"$parentPath/#text[$siblingIdx]",
          tag = "#text",
          nodeType = NodeType.Text,
          attributes = Map.empty,
          depth = depth,
          siblingIndex = siblingIdx,
          childCount = 0,
          textContent = Some(truncated),
          component = None,
          children = Vector.empty
        )

        (treeNode, TreeStats.forText(depth))

      case Node.Fragment(nodes) =>
        val (childNodes, childStats) = nodes.zipWithIndex.foldLeft(
          (Vector.empty[TreeNode], TreeStats.empty)
        ) { case ((acc, stats), (child, idx)) =>
          val (childNode, childStat) = buildNode(child, parentPath, depth, idx)
          (acc :+ childNode, stats + childStat)
        }

        val treeNode = TreeNode(
          id = s"$parentPath/#fragment[$siblingIdx]",
          tag = "#fragment",
          nodeType = NodeType.Fragment,
          attributes = Map.empty,
          depth = depth,
          siblingIndex = siblingIdx,
          childCount = nodes.size,
          textContent = None,
          component = None,
          children = childNodes
        )

        (treeNode, childStats)

  private def attrsToMap(attrs: List[Attr]): Map[String, String] =
    attrs.flatMap {
      case Attr.Id(v)          => Some("id" -> v)
      case Attr.Class(v)       => Some("class" -> v)
      case Attr.Href(v)        => Some("href" -> v)
      case Attr.Src(v)         => Some("src" -> v)
      case Attr.Alt(v)         => Some("alt" -> v)
      case Attr.Title(v)       => Some("title" -> v)
      case Attr.Name(v)        => Some("name" -> v)
      case Attr.Value(v)       => Some("value" -> v)
      case Attr.Type(v)        => Some("type" -> v)
      case Attr.Placeholder(v) => Some("placeholder" -> v)
      case Attr.For(v)         => Some("for" -> v)
      case Attr.Action(v)      => Some("action" -> v)
      case Attr.Method(v)      => Some("method" -> v)
      case Attr.Enctype(v)     => Some("enctype" -> v)
      case Attr.Target(v)      => Some("target" -> v)
      case Attr.Rel(v)         => Some("rel" -> v)
      case Attr.Role(v)        => Some("role" -> v)
      case Attr.Width(v)       => Some("width" -> v)
      case Attr.Height(v)      => Some("height" -> v)
      case Attr.Lang(v)        => Some("lang" -> v)
      case Attr.Dir(v)         => Some("dir" -> v)
      case Attr.Tabindex(v)    => Some("tabindex" -> v)
      case Attr.Colspan(v)     => Some("colspan" -> v)
      case Attr.Rowspan(v)     => Some("rowspan" -> v)
      case Attr.Loading(v)     => Some("loading" -> v)
      case Attr.Disabled       => Some("disabled" -> "true")
      case Attr.Checked        => Some("checked" -> "true")
      case Attr.Required       => Some("required" -> "true")
      case Attr.Readonly       => Some("readonly" -> "true")
      case Attr.Hidden         => Some("hidden" -> "true")
      case Attr.Autofocus      => Some("autofocus" -> "true")
      case Attr.Multiple       => Some("multiple" -> "true")
      case Attr.Selected       => Some("selected" -> "true")
      case Attr.Aria(n, v)     => Some(s"aria-$n" -> v)
      case Attr.Data(n, v)     => Some(s"data-$n" -> v)
      case Attr.Style(p, v)    => Some("style" -> s"$p: $v")
      case Attr.Custom(n, v)   => Some(n -> v)
      case _                   => None
    }.toMap

  private def extractComponent(attrs: List[Attr]): Option[ComponentMeta] =
    val componentName = attrs.collectFirst { case Attr.Data("component", v) =>
      v
    }
    val componentCategory =
      attrs
        .collectFirst { case Attr.Data("component-category", v) => v }
        .getOrElse("default")
    componentName.map(name => ComponentMeta(name, componentCategory))

  private def extractTextContent(children: Vector[Node]): Option[String] =
    children match
      case Vector(Node.Text(content)) =>
        Some(if content.length > 50 then content.take(47) + "..." else content)
      case _ => None
