package html.lib.table

import tags.i18n.I18n

sealed trait TableBuilderState
sealed trait TStart extends TableBuilderState
sealed trait TCaption extends TableBuilderState
sealed trait TColgroup extends TableBuilderState
sealed trait THeader extends TableBuilderState
sealed trait TBody extends TableBuilderState
sealed trait TFooter extends TableBuilderState

extension (i: Table.Init)
  def --(value: I18n[?]): Table.Caption =
    Table.Caption(i.prefix, value)

extension (h: Table.Head)
  def |+(value: I18n[?]): Table.Head =
    Table.Head(
      h.prefix,
      h.rows :+ HRow(Vector(Cell(value))),
      h.caption,
      h.colGroup
    )

  def |+(cell: Cell): Table.Head =
    Table.Head(h.prefix, h.rows :+ HRow(Vector(cell)), h.caption, h.colGroup)

  def |(value: I18n[?]): Table.Head =
    val r = h.rows.last
    val cols = r.columns :+ Cell(value)
    Table.Head(
      prefix = h.prefix,
      rows = h.rows.dropRight(1) :+ HRow(cols),
      caption = h.caption,
      colGroup = h.colGroup
    )

  def |(cell: Cell): Table.Head =
    val r = h.rows.last
    val cols = r.columns :+ cell
    Table.Head(
      prefix = h.prefix,
      rows = h.rows.dropRight(1) :+ HRow(cols),
      caption = h.caption,
      colGroup = h.colGroup
    )
extension (c: Table.ColGroup)
  def |(value: I18n[?]): Table.ColGroup =
    Table.ColGroup(c.prefix, c.columns :+ Col(value), c.caption)

extension (b: Table.Body)
  def |+(value: I18n[?]): Table.Body =
    Table.Body(
      b.prefix,
      b.rows :+ Row(Vector(Cell(value))),
      b.head,
      b.caption,
      b.colGroup
    )

  def |+(cell: Cell): Table.Body =
    Table.Body(
      b.prefix,
      b.rows :+ Row(Vector(cell)),
      b.head,
      b.caption,
      b.colGroup
    )

  def |(value: I18n[?]): Table.Body =
    val r = b.rows.last
    val cells = r.cells :+ Cell(value)
    Table.Body(
      prefix = b.prefix,
      rows = b.rows.dropRight(1) :+ Row(cells),
      head = b.head,
      caption = b.caption,
      colGroup = b.colGroup
    )

  def |(cell: Cell): Table.Body =
    val r = b.rows.last
    val cells = r.cells :+ cell
    Table.Body(
      prefix = b.prefix,
      rows = b.rows.dropRight(1) :+ Row(cells),
      head = b.head,
      caption = b.caption,
      colGroup = b.colGroup
    )

final case class Col(text: I18n[?], spanNrOfColumns: Option[String] = None)
final case class Cell(
    content: I18n[?],
    width: Option[Int] = None,
    colspan: Option[Int] = None,
    rowspan: Option[Int] = None
)
final case class Row(cells: Vector[Cell])
final case class HRow(columns: Vector[Cell])

extension [K <: String](i: I18n[K])
  /** Create a cell with `colspan` attribute */
  def spanning(n: Int): Cell = Cell(i, colspan = Some(n))

  /** Create a cell with `rowspan` attribute */
  def rowSpanning(n: Int): Cell = Cell(i, rowspan = Some(n))

extension (c: Cell)
  /** Add `colspan` attribute to a cell */
  def spanning(n: Int): Cell = c.copy(colspan = Some(n))

  /** Add `rowspan` attribute to a cell */
  def rowSpanning(n: Int): Cell = c.copy(rowspan = Some(n))

given CanEqual[Table[?], Table[?]] = CanEqual.derived

enum Table[S <: TableBuilderState]:
  self =>
  case Init(prefix: String) extends Table[TStart]
  case Caption(prefix: String, value: I18n[?]) extends Table[TCaption]
  case ColGroup(
      prefix: String,
      columns: Vector[Col],
      caption: Option[Caption] = None
  ) extends Table[TColgroup]
  case Head(
      prefix: String,
      rows: Vector[HRow],
      caption: Option[Caption] = None,
      colGroup: Option[ColGroup] = None
  ) extends Table[THeader]
  case Body(
      prefix: String,
      rows: Vector[Row],
      head: Option[Head] = None,
      caption: Option[Caption] = None,
      colGroup: Option[ColGroup] = None
  ) extends Table[TBody]

  case Footer(
      prefix: String,
      text: I18n[?],
      body: Option[Body] = None,
      head: Option[Head] = None,
      caption: Option[Caption] = None,
      colGroup: Option[ColGroup] = None
  ) extends Table[TFooter]

  def |*(value: I18n[?])(using
      ev: CanAdd.Aux[S, AddColGroup, ?, TColgroup]
  ): Table.ColGroup =
    self match
      case cg: ColGroup =>
        Table.ColGroup(cg.prefix, cg.columns :+ Col(value), cg.caption)
      case Init(prefix) =>
        Table.ColGroup(prefix, Vector(Col(value)))
      case c: Caption =>
        Table.ColGroup(c.prefix, Vector(Col(value)), Some(c))
      case _ => throw new Exception(s"Invalid state for |* operator: $self")

  def |-(value: I18n[?])(using
      ev: CanAdd.Aux[S, AddHeader, ?, THeader]
  ): Table.Head =
    self.|-(Cell(value))

  def |-(cell: Cell)(using
      ev: CanAdd.Aux[S, AddHeader, ?, THeader]
  ): Table.Head =
    self match
      case c: Caption =>
        Table.Head(c.prefix, Vector(HRow(Vector(cell))), Some(c))
      case Init(prefix) =>
        Table.Head(prefix, Vector(HRow(Vector(cell))), None)
      case cg: ColGroup =>
        Table.Head(cg.prefix, Vector(HRow(Vector(cell))), cg.caption, Some(cg))
      case _ => throw new Exception(s"Invalid state for |- operator: $self")

  def ||(value: I18n[?])(using
      ev: CanAdd.Aux[S, AddBody, ?, TBody]
  ): Table.Body =
    self.||(Cell(value))

  def ||(cell: Cell)(using
      ev: CanAdd.Aux[S, AddBody, ?, TBody]
  ): Table.Body =
    self match
      case h: Table.Head =>
        Table.Body(
          h.prefix,
          Vector(Row(Vector(cell))),
          Option(h),
          h.caption,
          h.colGroup
        )
      case _ => throw new Exception(s"Invalid state for || operator: $self")

  def |^(value: I18n[?])(using
      ev: CanAdd.Aux[S, AddFooter, ?, TFooter]
  ): Table.Footer =
    self match
      case b: Table.Body =>
        Table.Footer(b.prefix, value, Option(b), b.head, b.caption, b.colGroup)
      case _ => throw new Exception(s"Invalid state for |^ operator: $self")

object Table:

  def apply(prefix: String): Table.Init = Table.Init(prefix)
