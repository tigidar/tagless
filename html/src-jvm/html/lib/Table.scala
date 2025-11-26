package html.lib

import tags.T.*
import tagless.dsl.{given, *}

sealed trait TableState
sealed trait I extends TableState
sealed trait C extends TableState
sealed trait CG extends TableState
sealed trait H extends TableState
sealed trait B extends TableState
sealed trait F extends TableState

extension (i: Table.Init.type)
  def --(value: String): Table.Caption =
    Table.Caption(value)

extension (h: Table.Head)
  def |+(value: String): Table.Head =
    Table.Head(h.rows :+ Table.HRow(Vector(Col(value))), h.caption)

  def |(value: String): Table.Head =
    val r = h.rows.last
    val cols = r.columns :+ Col(value)
    h.copy(
      rows = h.rows.dropRight(1) :+ Table.HRow(cols)
    )

extension (b: Table.Body)
  def |+(value: String): Table.Body =
    Table.Body(b.rows :+ Table.Row(Vector(Cell(value))), b.head, b.caption)

  def |(value: String): Table.Body =
    val r = b.rows.last
    val cells = r.cells :+ Cell(value)
    b.copy(
      rows = b.rows.dropRight(1) :+ Table.Row(cells)
    )

final case class Col(text: String, span: Option[String] = None)
final case class Cell(content: String, width: Option[Int] = None)

enum Table derives CanEqual:
  self =>
  case Init
  case Caption(value: String)
  case HRow(columns: Vector[Col])
  case Head(rows: Vector[HRow], caption: Option[Caption] = None)
  case Row(cells: Vector[Cell])
  case Body(
      rows: Vector[Row],
      head: Option[Head] = None,
      caption: Option[Caption] = None
  )

  def |-(value: String): Table.Head =
    self match
      case Init => Table.Head(Vector(HRow(Vector(Col(value)))), None)
      case Caption(value) =>
        Table.Head(Vector(HRow(Vector(Col(value)))), Some(Caption(value)))
      case _ => throw new Exception(s"Invalid state for |- operator: $self")

  def ||(value: String): Table.Head = self match
    case h: Table.Head =>
      Table.Head(h.rows :+ HRow(Vector(Col(value))), h.caption)
    case _ => throw new Exception(s"Invalid state for |* operator: $self")

object Table:

  def ^ : Table.Init.type = Table.Init

object Test:

  def t = Table.^
    -- "Caption for some table"
    |- "td00" | "td01" | "td02"
    |+ "td10" | "td11" | "td12"
    || "td10" | "td11" | "td12"
    |+ "td20" | "td21" | "td22"
    |+ "td30" | "td31" | "td32"
    || "td40" | "td41" | "td42"
