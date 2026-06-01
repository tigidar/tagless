package tags.viz

/** Serializes TreeNode to D3.js-compatible JSON format */
object D3Serializer:

  /** Serialize a TreeNode to D3-compatible JSON string */
  def serialize(node: TreeNode): String =
    val sb = new StringBuilder
    serializeNode(node, sb, indent = 0)
    sb.toString

  private def serializeNode(
      node: TreeNode,
      sb: StringBuilder,
      indent: Int
  ): Unit =
    val pad = "  " * indent
    sb.append(s"$pad{\n")

    // Required D3 hierarchy fields
    sb.append(s"""$pad  "name": "${escapeJson(node.label)}",\n""")
    sb.append(s"""$pad  "tagName": "${escapeJson(node.tag)}",\n""")
    sb.append(s"""$pad  "nodeType": "${node.nodeType}",\n""")
    sb.append(s"""$pad  "depth": ${node.depth},\n""")

    // Optional fields
    node.textContent.foreach { text =>
      sb.append(s"""$pad  "text": "${escapeJson(text)}",\n""")
    }

    node.component.foreach { comp =>
      sb.append(s"""$pad  "component": {\n""")
      sb.append(s"""$pad    "name": "${escapeJson(comp.name)}",\n""")
      sb.append(s"""$pad    "category": "${escapeJson(comp.category)}"\n""")
      sb.append(s"""$pad  },\n""")
    }

    // Attributes as object
    if node.attributes.nonEmpty then
      sb.append(s"""$pad  "attributes": {\n""")
      val attrEntries = node.attributes.toList
      attrEntries.zipWithIndex.foreach { case ((key, value), idx) =>
        val comma = if idx < attrEntries.size - 1 then "," else ""
        sb.append(s"""$pad    "${escapeJson(key)}": "${escapeJson(
            value
          )}"$comma\n""")
      }
      sb.append(s"""$pad  },\n""")

    // Children array
    sb.append(s"""$pad  "children": [\n""")
    node.children.zipWithIndex.foreach { case (child, idx) =>
      serializeNode(child, sb, indent + 2)
      if idx < node.children.size - 1 then sb.append(",")
      sb.append("\n")
    }
    sb.append(s"""$pad  ]\n""")

    sb.append(s"$pad}")

  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
