package tags.viz

/** Renders TreeNode as ASCII tree diagram */
object AsciiTreeRenderer:

  /** Render a TreeNode as ASCII art tree */
  def render(node: TreeNode): String =
    val sb = new StringBuilder
    renderNode(node, sb, prefix = "", isLast = true, isRoot = true)
    sb.toString

  private def renderNode(
      node: TreeNode,
      sb: StringBuilder,
      prefix: String,
      isLast: Boolean,
      isRoot: Boolean
  ): Unit =
    // Draw the connector
    val connector = if isRoot then "" else if isLast then "└── " else "├── "

    // Build the label
    val label = node.nodeType match
      case NodeType.Text =>
        val text = node.textContent.getOrElse("")
        s"#text: \"$text\""
      case NodeType.Fragment =>
        s"#fragment (${node.childCount} nodes)"
      case NodeType.Component =>
        val comp = node.component.map(c => s" @${c.name}").getOrElse("")
        s"${node.shortLabel}$comp"
      case _ =>
        node.shortLabel

    sb.append(s"$prefix$connector$label\n")

    // Calculate new prefix for children
    val newPrefix =
      if isRoot then "" else prefix + (if isLast then "    " else "│   ")

    // Render children
    node.children.zipWithIndex.foreach { case (child, idx) =>
      val childIsLast = idx == node.children.size - 1
      renderNode(child, sb, newPrefix, childIsLast, isRoot = false)
    }
