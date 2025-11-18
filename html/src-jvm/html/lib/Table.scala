package html.lib

import tags.T.*
import tagless.dsl.{given, *}

final case class FullTable(
    caption: Option[Caption] = None,
    thead: Option[TableHead] = None,
    tbody: Option[TableBody] = None,
    tfoot: Option[TableFoot] = None
)

final case class Caption(value: String)
final case class TableHead(rows: List[TableRow])
final case class TableBody(rows: List[TableRow])
final case class TableFoot(rows: List[TableRow])
final case class TableRow(cells: List[TableCell])
final case class TableCell(content: String, isHeader: Boolean = false)

object Table:

  def renderTable(fullTable: FullTable) =
    ~table
      >> fullTable.caption
        .map(c => captionTag(c.value))
        .getOrElse(emptyTag)
      >> table.thead
        .map(th =>
          theadTag
            >> th.rows.map(r =>
              trTag
                >> r.cells.map(c =>
                  if c.isHeader then thTag(c.content)
                  else tdTag(c.content)
                )
            )
        )
        .getOrElse(emptyTag)
      >> table.tbody
        .map(tb =>
          tbodyTag
            >> tb.rows.map(r =>
              trTag
                >> r.cells.map(c =>
                  if c.isHeader then thTag(c.content)
                  else tdTag(c.content)
                )
            )
        )
        .getOrElse(emptyTag)
      >> table.tfoot
        .map(tf =>
          tfootTag
            >> tf.rows.map(r =>
              trTag
                >> r.cells.map(c =>
                  if c.isHeader then thTag(c.content)
                  else tdTag(c.content)
                )
            )
        )
        .getOrElse(emptyTag)
