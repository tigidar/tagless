package tags.viz

/** Renders TreeNode as Mermaid diagram syntax */
object MermaidRenderer:

  /** Render a TreeNode as Mermaid flowchart */
  def render(node: TreeNode): String =
    val sb = new StringBuilder
    sb.append("flowchart TD\n")

    var nodeCounter = 0
    def nextId(): String =
      nodeCounter += 1
      s"n$nodeCounter"

    def renderNode(node: TreeNode, parentId: Option[String]): String =
      val nodeId = nextId()

      // Determine node shape based on type
      val (openBracket, closeBracket, label) = node.nodeType match
        case NodeType.Component =>
          val comp = node.component.map(_.name).getOrElse("?")
          ("[[", "]]", s"${node.tag}<br/>@$comp")
        case NodeType.Text =>
          val text = node.textContent.getOrElse("").take(20)
          (">", "]", s"\"$text\"")
        case NodeType.Fragment =>
          ("{{", "}}", s"fragment")
        case NodeType.Void =>
          ("(", ")", node.tag)
        case NodeType.Element =>
          val id = node.attributes.get("id").map(i => s"#$i").getOrElse("")
          ("[", "]", s"${node.tag}$id")

      // Add node definition
      sb.append(s"    $nodeId$openBracket$label$closeBracket\n")

      // Add edge from parent
      parentId.foreach { pid =>
        sb.append(s"    $pid --> $nodeId\n")
      }

      // Render children
      node.children.foreach(child => renderNode(child, Some(nodeId)))

      nodeId

    val _ = renderNode(node, None)
    sb.toString
