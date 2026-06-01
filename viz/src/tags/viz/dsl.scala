package tags.viz

import scala.annotation.targetName
import tags.{Attr, Cursor, Depth, ElementKind, Node}

/** Component-marking and tree-visualization extensions.
  *
  * Import alongside `tags.dsl.*`:
  *
  * {{{
  * import tags.dsl.*
  * import tags.viz.dsl.*
  * }}}
  */
object dsl:

  /** Mark a cursor's root element as a component boundary.
    *
    * Adds `data-component` and `data-component-category` attributes; consumed
    * by `TreeVisualizer.extractComponent`.
    */
  extension [D <: Depth, K <: ElementKind](cursor: Cursor[D, K])
    def asComponent(name: String, category: String = "default"): Cursor[D, K] =
      cursor.focus match
        case elem: Node.Element =>
          val newAttrs = elem.attrs :+
            Attr.Data("component", name) :+
            Attr.Data("component-category", category)
          cursor.copy(focus = elem.copy(attrs = newAttrs))
        case _ => cursor

    /** Build a visualization tree from the cursor */
    def visualize: TreeVisualization =
      TreeVisualizer.build(cursor.seal)

    /** Render as D3.js-compatible JSON */
    def toD3Json: String =
      visualize.toD3Json

    /** Render as ASCII tree diagram */
    def toAsciiTree: String =
      visualize.toAsciiTree

    /** Render as Mermaid flowchart */
    def toMermaid: String =
      visualize.toMermaid

  extension (node: Node)
    @targetName("nodeVisualize")
    def visualize: TreeVisualization =
      TreeVisualizer.build(node)

    @targetName("nodeToD3Json")
    def toD3Json: String =
      visualize.toD3Json

    @targetName("nodeToAsciiTree")
    def toAsciiTree: String =
      visualize.toAsciiTree

    @targetName("nodeToMermaid")
    def toMermaid: String =
      visualize.toMermaid
