package tags.render

import tags.*

/** HTML string renderer for Node trees. */
object Html:

  /** Render a node to HTML string */
  def render(node: Node): String = node match
    case Node.Element(tag, attrs, children) =>
      val attrStr = Attr.renderAll(attrs)
      if children.isEmpty then s"<$tag$attrStr></$tag>"
      else
        val childrenStr = children.map(render).mkString
        s"<$tag$attrStr>$childrenStr</$tag>"

    case Node.VoidElement(tag, attrs) =>
      val attrStr = Attr.renderAll(attrs)
      s"<$tag$attrStr />"

    case Node.Text(content) =>
      escapeHtml(content)

    case Node.Fragment(nodes) =>
      nodes.map(render).mkString

  /** Escape HTML special characters */
  private def escapeHtml(s: String): String =
    s.flatMap {
      case '<'  => "&lt;"
      case '>'  => "&gt;"
      case '&'  => "&amp;"
      case '"'  => "&quot;"
      case '\'' => "&#39;"
      case c    => c.toString
    }
