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

final case class Col(cssClassName: String, span: Option[String] = None)
final case class Cell(content: String, width: Option[Int] = None)

enum Table[S <: TableState] derives CanEqual:
  self =>
  case Init extends Table[I]
  case Caption(value: String) extends Table[C]
  case ColGroup(col: List[Col]) extends Table[CG]
  case HRow[S1 <: TableState](cells: Vector[Cell]) extends Table[S1]
  case Row[S1 <: TableState](cells: Vector[Cell]) extends Table[S1]
  case Head(rows: Vector[HRow[S]], caption: Option[Caption]) extends Table[H]
  case Body(rows: Vector[Row[S]], h: Option[Head[H]], caption: Option[Caption])
      extends Table[B]
  case Foot(rows: Vector[Row[S]]) extends Table[F]
  case Append(l: Table[S], r: Table[S])

  def --(captionId: String)(using ev: S =:= I): Table[C] =
    Caption(captionId)

  // only on Init OR Caption
  type PreHead = I | C

  def getCaption: Option[Caption] = self match
    case c: Caption => Some(c)
    case _          => None

  def -|(using ev: S <:< PreHead)(id: String): Table.Head[H] =
    Head(Vector(HRow(Vector(Cell(id)))), getCaption)

  def +|(id: String): Table.Body[B] =
    Table.Body(Vector(Table.Row(Vector(Cell(id)))), None, getCaption)

extension [S1 <: TableState](r: Table.Row[S1])
  def +(c: Cell): Table.Row[S1] = r.copy(
    cells = r.cells :+ c
  )

extension (h: Table.Head[H])

  def +(id: String): Table.Head[H] =
    Table.Head(h.rows :+ Table.HRow(Vector(Cell(id))), h.caption)

  def |(id: String): Table.Head[H] =
    val r = h.rows.last
    val cells = r.cells :+ Cell(id)
    h.copy(
      rows = h.rows.dropRight(1) :+ Table.HRow(cells)
    )

  def ||(id: String): Table.Body[B] =
    Table.Body(Vector(Table.Row(Vector(Cell(id)))), Some(h), None)

extension (b: Table.Body[B])
  def +(id: String): Table.Body[B] =
    b.copy(rows = b.rows :+ Table.Row(Vector(Cell(id))))

  def |(id: String): Table.Body[B] =
    val r = b.rows.last
    val cells = r.cells :+ Cell(id)
    b.copy(
      rows = b.rows.dropRight(1) :+ Table.Row(cells)
    )

object Table:

  def ^ = Table.Init

  def renderTable[S <: TableState](fullTable: Table[S]): String = ???

  // format: off
object Test:

  def t = Table.^
    -- "Caption for some table"
    -| "td00"  | "td01"  | "td02"
     + "td10"  | "td11"  | "td12"
    || "col00" | "col01" | "col02"
     + "col10" | "col11" | "col12"
     + "col20" | "col21" | "col22"
     + "col30" | "col31" | "col32"
     + "col40" | "col41" | "col42"


  def t2 = Table.^
    -- "Caption two"
    +| "col00" | "col01" | "col02"
     + "col10" | "col11" | "col12"
     + "col20" | "col21" | "col22"
     + "col30" | "col31" | "col32"
     + "col40" | "col41" | "col42"


  def t3 = Table.^
    +| "col00" | "col01" | "col02"
     + "col10" | "col11" | "col12"
     + "col20" | "col21" | "col22"
     + "col30" | "col31" | "col32"
     + "col40" | "col41" | "col42"

  def t4 = Table.^
    -| "td00"  | "td01"  | "td02"
     + "td10"  | "td11"  | "td12"
    || "col00" | "col01" | "col02"
     + "col10" | "col11" | "col12"
     + "col20" | "col21" | "col22"
     + "col30" | "col31" | "col32"
     + "col40" | "col41" | "col42"

