package tags

import munit.FunSuite
import _root_.html.lib.table.*
import _root_.html.lib.table.TableInterpreter
import tags.TestI18n.{given, *}

class TableSpec extends FunSuite:

  // ============================================================
  // Cell colspan/rowspan — data model
  // ============================================================

  test("Cell defaults have no colspan or rowspan"):
    val c = Cell("text".i18n)
    assertEquals(c.colspan, None)
    assertEquals(c.rowspan, None)

  test("I18n.spanning creates cell with colspan"):
    val c = "Total".i18n.spanning(3)
    assertEquals(c.content, "Total".i18n)
    assertEquals(c.colspan, Some(3))
    assertEquals(c.rowspan, None)

  test("I18n.rowSpanning creates cell with rowspan"):
    val c = "Category".i18n.rowSpanning(2)
    assertEquals(c.content, "Category".i18n)
    assertEquals(c.rowspan, Some(2))
    assertEquals(c.colspan, None)

  test("chaining spanning and rowSpanning"):
    val c = "Merged".i18n.spanning(2).rowSpanning(3)
    assertEquals(c.content, "Merged".i18n)
    assertEquals(c.colspan, Some(2))
    assertEquals(c.rowspan, Some(3))

  test("Cell.spanning extension"):
    val c = Cell("X".i18n).spanning(4)
    assertEquals(c.colspan, Some(4))

  test("Cell.rowSpanning extension"):
    val c = Cell("Y".i18n).rowSpanning(5)
    assertEquals(c.rowspan, Some(5))

  // ============================================================
  // Table DSL with cells — operators accept Cell
  // ============================================================

  test("header | accepts Cell with colspan"):
    val t = Table("t")
      |- "Name".i18n | "Details".i18n.spanning(2)
      || "Alice".i18n | "30".i18n | "NYC".i18n
    t match
      case b: Table.Body =>
        val headerRow = b.head.get.rows.head
        assertEquals(headerRow.columns(1).colspan, Some(2))
      case _ => fail("Expected Body")

  test("header |+ accepts Cell"):
    val t = Table("t")
      |- "A".i18n | "B".i18n
      |+ "C".i18n.spanning(2)
      || "x".i18n | "y".i18n
    t match
      case b: Table.Body =>
        val secondRow = b.head.get.rows(1)
        assertEquals(secondRow.columns(0).colspan, Some(2))
      case _ => fail("Expected Body")

  test("body | accepts Cell with rowspan"):
    val t = Table("t")
      |- "Name".i18n | "Value".i18n
      || "Group".i18n.rowSpanning(2) | "A".i18n
      |+ "B".i18n
    t match
      case b: Table.Body =>
        assertEquals(b.rows(0).cells(0).rowspan, Some(2))
        assertEquals(b.rows(0).cells(0).content, "Group".i18n)
      case _ => fail("Expected Body")

  test("body |+ accepts Cell"):
    val t = Table("t")
      |- "A".i18n | "B".i18n
      || "x".i18n | "y".i18n
      |+ "Total".i18n.spanning(2)
    t match
      case b: Table.Body =>
        assertEquals(b.rows(1).cells(0).colspan, Some(2))
      case _ => fail("Expected Body")

  test("|- accepts Cell"):
    val t = Table("t")
      |- "Header".i18n.spanning(3)
      || "a".i18n | "b".i18n | "c".i18n
    t match
      case b: Table.Body =>
        val headerCell = b.head.get.rows.head.columns.head
        assertEquals(headerCell.colspan, Some(3))
      case _ => fail("Expected Body")

  test("|| accepts Cell"):
    val t = Table("t")
      |- "H1".i18n | "H2".i18n
      || "Total".i18n.spanning(2)
    t match
      case b: Table.Body =>
        assertEquals(b.rows.head.cells.head.colspan, Some(2))
      case _ => fail("Expected Body")

  // ============================================================
  // TableInterpreter — rendering with colspan/rowspan
  // ============================================================

  test("rendered table has colspan on th"):
    val t = Table("t")
      |- "Name".i18n | "Details".i18n.spanning(2)
      || "Alice".i18n | "30".i18n | "NYC".i18n
    val node = TableInterpreter.toNode(t)
    val html = tags.render.Html.render(node)
    assert(
      html.contains("""<th colspan="2">Details</th>"""),
      s"Missing colspan on th in: $html"
    )

  test("rendered table has rowspan on td"):
    val t = Table("t")
      |- "Name".i18n | "Value".i18n
      || "Group".i18n.rowSpanning(2) | "A".i18n
      |+ "B".i18n
    val node = TableInterpreter.toNode(t)
    val html = tags.render.Html.render(node)
    assert(
      html.contains("""<td rowspan="2">Group</td>"""),
      s"Missing rowspan on td in: $html"
    )

  test("rendered table has both colspan and rowspan on td"):
    val t = Table("t")
      |- "A".i18n | "B".i18n | "C".i18n
      || "Merged".i18n.spanning(2).rowSpanning(3) | "X".i18n
    val node = TableInterpreter.toNode(t)
    val html = tags.render.Html.render(node)
    assert(html.contains("""colspan="2""""), s"Missing colspan in: $html")
    assert(html.contains("""rowspan="3""""), s"Missing rowspan in: $html")

  test("cells without spans render without span attributes"):
    val t = Table("t")
      |- "A".i18n | "B".i18n
      || "x".i18n | "y".i18n
    val node = TableInterpreter.toNode(t)
    val html = tags.render.Html.render(node)
    assert(!html.contains("colspan"), s"Unexpected colspan in: $html")
    assert(!html.contains("rowspan"), s"Unexpected rowspan in: $html")

  test("complete table with spans renders full structure"):
    val t = Table("report")
      |- "Quarter".i18n.rowSpanning(2) | "Revenue".i18n.spanning(2)
      |+ "Q1".i18n | "Q2".i18n
      || "2024".i18n | "100".i18n | "200".i18n
      |+ "2025".i18n | "150".i18n | "250".i18n
    val node = TableInterpreter.toNode(t)
    val html = tags.render.Html.render(node)
    assert(
      html.contains("""id="report-table""""),
      s"Missing table id in: $html"
    )
    assert(
      html.contains("""<th rowspan="2">Quarter</th>"""),
      s"Missing rowspan header in: $html"
    )
    assert(
      html.contains("""<th colspan="2">Revenue</th>"""),
      s"Missing colspan header in: $html"
    )
    assert(html.contains("<td>100</td>"), s"Missing body cell in: $html")
