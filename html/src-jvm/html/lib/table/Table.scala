package html.lib.table

import tags.T.*
import tagless.dsl.{given, *}

sealed trait TableBuilderState
sealed trait TStart extends TableBuilderState
sealed trait TCaption extends TableBuilderState
sealed trait TColgroup extends TableBuilderState
sealed trait THeader extends TableBuilderState
sealed trait TBody extends TableBuilderState
sealed trait TFooter extends TableBuilderState

extension (i: Table.Init.type)
  def --(value: String): Table.Caption =
    Table.Caption(value)

extension (h: Table.Head)
  def |+(value: String): Table.Head =
    Table.Head(h.rows :+ HRow(Vector(Cell(value))), h.caption)

  def |(value: String): Table.Head =
    val r = h.rows.last
    val cols = r.columns :+ Cell(value)
    h.copy(
      rows = h.rows.dropRight(1) :+ HRow(cols)
    )
extension (c: Table.ColGroup)
  def |(value: String): Table.ColGroup =
    Table.ColGroup(c.columns :+ Col(value))

extension (b: Table.Body)
  def |+(value: String): Table.Body =
    Table.Body(b.rows :+ Row(Vector(Cell(value))), b.head, b.caption)

  def |(value: String): Table.Body =
    val r = b.rows.last
    val cells = r.cells :+ Cell(value)
    b.copy(
      rows = b.rows.dropRight(1) :+ Row(cells)
    )

final case class Col(text: String, spanNrOfColumns: Option[String] = None)
final case class Cell(content: String, width: Option[Int] = None)
final case class Row(cells: Vector[Cell])
final case class HRow(columns: Vector[Cell])

given CanEqual[TableBuilderState, TableBuilderState] = CanEqual.derived

enum Table[S <: TableBuilderState] derives CanEqual:
  self =>
  case Init extends Table[TStart]
  case Caption(value: String) extends Table[TCaption]
  case ColGroup(columns: Vector[Col]) extends Table[TColgroup]
  case Head(rows: Vector[HRow], caption: Option[Caption] = None)
      extends Table[THeader]
  case Body(
      rows: Vector[Row],
      head: Option[Head] = None,
      caption: Option[Caption] = None
  ) extends Table[TBody]

  case Footer(
      text: String,
      body: Option[Body] = None,
      head: Option[Head] = None,
      caption: Option[Caption] = None
  ) extends Table[TFooter]

  def |*(value: String)(using
      ev: CanAdd.Aux[S, AddColGroup, ?, TColgroup]
  ): Table.ColGroup =
    self match
      case ColGroup(cols) =>
        Table.ColGroup(cols :+ Col(value))
      case _ => throw new Exception(s"Invalid state for |* operator: $self")

  def |-(value: String)(using
      ev: CanAdd.Aux[S, AddHeader, ?, THeader]
  ): Table.Head =
    self match
      case cap @ Caption(captionValue) =>
        Table.Head(Vector(HRow(Vector(Cell(value)))), Some(cap))
      case Init => Table.Head(Vector(HRow(Vector(Cell(value)))), None)
      case _    => throw new Exception(s"Invalid state for |- operator: $self")

  def ||(value: String)(using
      ev: CanAdd.Aux[S, AddBody, ?, TBody]
  ): Table.Body =
    self match
      case h: Table.Head =>
        Table.Body(Vector(Row(Vector(Cell(value)))), Option(h), h.caption)
      case _ => throw new Exception(s"Invalid state for |* operator: $self")

  def |^(value: String)(using
      ev: CanAdd.Aux[S, AddFooter, ?, TFooter]
  ): Table.Footer =
    self match
      case b: Table.Body =>
        Table.Footer(value, Option(b), b.head, b.caption)
      case _ => throw new Exception(s"Invalid state for |+ operator: $self")

object Table:

  def ^ : Table.Init.type = Table.Init

object Test:
// format: off
  def t = Table.^
    -- "Caption for some table" // add caption
    |* "col 1" | "col 2" | "col 3" // add colgroup
    |- "td00"  | "td01"  | "td02" // add header row 1
    |+ "td10"  | "td11"  | "td12" // add header row 2
    || "td10"  | "td11"  | "td12" // add table body row 1
    |+ "td20"  | "td21"  | "td22" // add table body row 2
    |+ "td30"  | "td31"  | "td32" // add table body row 3
    |^ "This is the footer" // add footer
