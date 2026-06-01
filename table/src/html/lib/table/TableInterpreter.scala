package html.lib.table

import tags.{Node, Attr}
import tags.i18n.{I18n, Lang}
import scala.language.implicitConversions

/** Interpreter that converts Table DSL to Node for integration with the HTML
  * DSL.
  *
  * Only complete tables (TBody or TFooter) can be converted. This is enforced
  * at compile time via the union type constraint.
  *
  * Usage:
  * {{{
  * import html.lib.table.*
  * import html.lib.table.TableInterpreter.given
  * import tags.dsl.*
  *
  * val table = Table("users")
  *   |- "Name" | "Age"
  *   || "Alice" | "30"
  *   |+ "Bob" | "25"
  *
  * div.toCursor >>^ table  // Auto-converts to Node
  * }}}
  */
object TableInterpreter:

  /** Convert a complete table to a Node.Element */
  def toNode(table: Table[TBody] | Table[TFooter])(using Lang): Node.Element =
    table match
      case b: Table.Body =>
        buildTable(b.prefix, b.caption, b.colGroup, b.head, Some(b), None)
      case f: Table.Footer =>
        buildTable(
          f.prefix,
          f.caption,
          f.colGroup,
          f.head,
          f.body,
          Some(f.text)
        )

  /** Implicit conversion for TBody tables */
  given tableBodyToNode(using Lang): Conversion[Table[TBody], Node] = toNode(_)

  /** Implicit conversion for TFooter tables */
  given tableFooterToNode(using Lang): Conversion[Table[TFooter], Node] =
    toNode(_)

  // ============================================================
  // Private builder methods
  // ============================================================

  private def buildTable(
      prefix: String,
      caption: Option[Table.Caption],
      colGroup: Option[Table.ColGroup],
      head: Option[Table.Head],
      body: Option[Table.Body],
      footer: Option[I18n[?]]
  )(using lang: Lang): Node.Element =
    val children = Vector.newBuilder[Node]

    // Add caption if present
    caption.foreach { c =>
      children += Node.Element("caption", Nil, Vector(Node.Text(c.value.apply)))
    }

    // Add colgroup if present
    colGroup.foreach { cg =>
      children += buildColgroup(cg)
    }

    // Add thead if present
    head.foreach { h =>
      children += buildThead(h)
    }

    // Add tbody if present
    body.foreach { b =>
      children += buildTbody(b)
    }

    // Add tfoot if present
    footer.foreach { text =>
      children += Node.Element(
        "tfoot",
        Nil,
        Vector(
          Node.Element(
            "tr",
            Nil,
            Vector(Node.Element("td", Nil, Vector(Node.Text(text.apply))))
          )
        )
      )
    }

    Node.Element("table", List(Attr.Id(s"$prefix-table")), children.result())

  private def buildColgroup(colGroup: Table.ColGroup): Node.Element =
    val cols = colGroup.columns.map { col =>
      val attrs = col.spanNrOfColumns match
        case Some(span) => List(Attr.Custom("span", span))
        case None       => Nil
      Node.VoidElement("col", attrs)
    }
    Node.Element("colgroup", Nil, cols)

  private def buildThead(head: Table.Head)(using lang: Lang): Node.Element =
    val rows = head.rows.map { hrow =>
      val cells = hrow.columns.map { cell =>
        Node.Element(
          "th",
          cellAttrs(cell),
          Vector(Node.Text(cell.content.apply))
        )
      }
      Node.Element("tr", Nil, cells)
    }
    Node.Element("thead", Nil, rows)

  private def buildTbody(body: Table.Body)(using lang: Lang): Node.Element =
    val rows = body.rows.map { row =>
      val cells = row.cells.map { cell =>
        Node.Element(
          "td",
          cellAttrs(cell),
          Vector(Node.Text(cell.content.apply))
        )
      }
      Node.Element("tr", Nil, cells)
    }
    Node.Element("tbody", Nil, rows)

  private def cellAttrs(cell: Cell): List[Attr] =
    val b = List.newBuilder[Attr]
    cell.width.foreach(w => b += Attr.Width(w.toString))
    cell.colspan.foreach(n => b += Attr.Colspan(n.toString))
    cell.rowspan.foreach(n => b += Attr.Rowspan(n.toString))
    b.result()
