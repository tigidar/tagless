package tags.render

import tags.*

/** Pretty-printed HTML renderer with configurable indentation.
  *
  * Produces indented, human-readable HTML output for development and debugging.
  *
  * Rendering rules:
  *   - Each element starts on its own line with appropriate indentation
  *   - Children are indented one level deeper than their parent
  *   - Void elements render on a single line
  *   - Text-only elements render inline: `<p>Hello</p>`
  *   - Fragment nodes render their children sequentially
  *   - Attributes stay on the same line as the opening tag
  */
object PrettyHtml:

  /** Render a node to indented HTML string with default 2-space indent */
  def render(node: Node): String =
    render(node, "  ")

  /** Render a node to indented HTML string with custom indent */
  def render(node: Node, indent: String): String =
    val sb = new StringBuilder
    renderNode(node, 0, indent, sb)
    sb.toString

  private def renderNode(
      node: Node,
      depth: Int,
      indent: String,
      sb: StringBuilder
  ): Unit =
    node match
      case Node.Element(tag, attrs, children) =>
        val attrStr = Attr.renderAll(attrs)
        if children.isEmpty then
          appendIndent(sb, depth, indent)
          sb.append(s"<$tag$attrStr></$tag>")
        else if isSingleTextChild(children) then
          // Inline text-only elements: <p>Hello</p>
          appendIndent(sb, depth, indent)
          val text = Html.render(children.head)
          sb.append(s"<$tag$attrStr>$text</$tag>")
        else
          appendIndent(sb, depth, indent)
          sb.append(s"<$tag$attrStr>")
          children.foreach { child =>
            sb.append('\n')
            renderNode(child, depth + 1, indent, sb)
          }
          sb.append('\n')
          appendIndent(sb, depth, indent)
          sb.append(s"</$tag>")

      case Node.VoidElement(tag, attrs) =>
        val attrStr = Attr.renderAll(attrs)
        appendIndent(sb, depth, indent)
        sb.append(s"<$tag$attrStr />")

      case Node.Text(content) =>
        appendIndent(sb, depth, indent)
        sb.append(escapeHtml(content))

      case Node.Fragment(nodes) =>
        nodes.zipWithIndex.foreach { (child, i) =>
          if i > 0 then sb.append('\n')
          renderNode(child, depth, indent, sb)
        }

  private def appendIndent(
      sb: StringBuilder,
      depth: Int,
      indent: String
  ): Unit =
    var i = 0
    while i < depth do
      sb.append(indent)
      i += 1

  private def isSingleTextChild(children: Vector[Node]): Boolean =
    children.size == 1 && children.head.isInstanceOf[Node.Text]

  /** Escape HTML special characters (shared logic with Html renderer) */
  private def escapeHtml(s: String): String =
    s.flatMap {
      case '<'  => "&lt;"
      case '>'  => "&gt;"
      case '&'  => "&amp;"
      case '"'  => "&quot;"
      case '\'' => "&#39;"
      case c    => c.toString
    }
