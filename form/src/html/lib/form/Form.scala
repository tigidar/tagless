package html.lib.form

import tags.i18n.I18n

// ============================================================
// State machine types
// ============================================================

sealed trait FormState
sealed trait FStart extends FormState
sealed trait FFields extends FormState
sealed trait FFieldset extends FormState
sealed trait FDone extends FormState

// ============================================================
// Field types and data
// ============================================================

// Marker traits for capability constraints
sealed trait CanBeRequired
sealed trait HasPattern
sealed trait HasMinMax
sealed trait HasPlaceholder

// Sealed trait for field types - each case object mixes in the relevant markers
sealed trait FieldType

object FieldType:
  case object Text
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Area extends FieldType with CanBeRequired with HasPlaceholder
  case object Number
      extends FieldType
      with CanBeRequired
      with HasMinMax
      with HasPlaceholder
  case object Email
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Password
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Hidden extends FieldType
  case object Tel
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Url
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Search
      extends FieldType
      with CanBeRequired
      with HasPattern
      with HasPlaceholder
  case object Date extends FieldType with CanBeRequired with HasMinMax
  case object Time extends FieldType with CanBeRequired with HasMinMax
  case object Color extends FieldType
  case object Range extends FieldType with HasMinMax
  case object File
      extends FieldType
      with CanBeRequired // Note: File does NOT have HasPlaceholder

final case class FieldAttrs(
    required: Boolean = false,
    pattern: Option[String] = None,
    min: Option[String] = None,
    max: Option[String] = None,
    placeholder: Option[String] = None
)

final case class Field[+T <: FieldType](
    label: I18n[?],
    fieldType: T,
    attrs: FieldAttrs = FieldAttrs()
)

// Form-level attributes for the <form> element itself
final case class FormConfig(
    action: Option[String] = None,
    method: Option[String] = None,
    enctype: Option[String] = None,
    novalidate: Boolean = false
)

final case class SelectField(label: I18n[?], options: Vector[I18n[?]])

final case class CheckboxGroup(label: I18n[?], options: Vector[I18n[?]])

/** Radio button group data */
final case class RadioGroup(label: I18n[?], options: Vector[I18n[?]])

/** Builder for radio button groups with fluent `?` operator */
final case class RadioBuilder(label: I18n[?], options: Vector[I18n[?]]):
  def ?(option: I18n[?]): RadioBuilder = copy(options = options :+ option)

final case class SelectBuilder(label: I18n[?], options: Vector[I18n[?]]):
  def ?(option: I18n[?]): SelectBuilder = copy(options = options :+ option)

enum FormEntry:
  case Input(field: Field[?])
  case Select(field: SelectField)
  case Checkbox(group: CheckboxGroup)
  case Radio(group: RadioGroup)
  case Fieldset(legend: I18n[?], entries: Vector[FormEntry])

// Type-safe extension methods for field validation attributes
extension [T <: FieldType & CanBeRequired](f: Field[T])
  def required: Field[T] = f.copy(attrs = f.attrs.copy(required = true))

extension [T <: FieldType & HasPattern](f: Field[T])
  def pattern(p: String): Field[T] =
    f.copy(attrs = f.attrs.copy(pattern = Some(p)))

extension [T <: FieldType & HasMinMax](f: Field[T])
  def min(v: String): Field[T] = f.copy(attrs = f.attrs.copy(min = Some(v)))
  def max(v: String): Field[T] = f.copy(attrs = f.attrs.copy(max = Some(v)))

extension [T <: FieldType & HasPlaceholder](f: Field[T])
  def placeholder(p: String): Field[T] =
    f.copy(attrs = f.attrs.copy(placeholder = Some(p)))

extension [K <: String](i: I18n[K])
  def txt: Field[FieldType.Text.type] = Field(i, FieldType.Text)
  def area: Field[FieldType.Area.type] = Field(i, FieldType.Area)
  def num: Field[FieldType.Number.type] = Field(i, FieldType.Number)
  def email: Field[FieldType.Email.type] = Field(i, FieldType.Email)
  def pwd: Field[FieldType.Password.type] = Field(i, FieldType.Password)
  def tel: Field[FieldType.Tel.type] = Field(i, FieldType.Tel)
  def url: Field[FieldType.Url.type] = Field(i, FieldType.Url)
  def search: Field[FieldType.Search.type] = Field(i, FieldType.Search)
  def date: Field[FieldType.Date.type] = Field(i, FieldType.Date)
  def time: Field[FieldType.Time.type] = Field(i, FieldType.Time)
  def color: Field[FieldType.Color.type] = Field(i, FieldType.Color)
  def range: Field[FieldType.Range.type] = Field(i, FieldType.Range)
  def file: Field[FieldType.File.type] = Field(i, FieldType.File)
  def ?(option: I18n[?]): SelectBuilder = SelectBuilder(i, Vector(option))

  /** Start building a radio button group with first option */
  def ??(option: I18n[?]): RadioBuilder = RadioBuilder(i, Vector(option))

// ============================================================
// Operators
// ============================================================

extension (init: Form.Init)
  def action(v: String): Form.Init =
    Form.Init(init.prefix, init.config.copy(action = Some(v)))
  def method(v: String): Form.Init =
    Form.Init(init.prefix, init.config.copy(method = Some(v)))
  def enctype(v: String): Form.Init =
    Form.Init(init.prefix, init.config.copy(enctype = Some(v)))
  def novalidate: Form.Init =
    Form.Init(init.prefix, init.config.copy(novalidate = true))
  def |>(field: Field[?]): Form.Fields =
    Form.Fields(init.prefix, init.config, Vector(FormEntry.Input(field)))
  def ||(sb: SelectBuilder): Form.Fields =
    Form.Fields(
      init.prefix,
      init.config,
      Vector(FormEntry.Select(SelectField(sb.label, sb.options)))
    )
  def |*(sb: SelectBuilder): Form.Fields =
    Form.Fields(
      init.prefix,
      init.config,
      Vector(FormEntry.Checkbox(CheckboxGroup(sb.label, sb.options)))
    )

  /** Add radio button group and transition to Fields state */
  def |@(rb: RadioBuilder): Form.Fields =
    Form.Fields(
      init.prefix,
      init.config,
      Vector(FormEntry.Radio(RadioGroup(rb.label, rb.options)))
    )
  def |>>(legend: I18n[?]): Form.InFieldset =
    Form.InFieldset(
      init.prefix,
      init.config,
      Vector.empty,
      legend,
      Vector.empty
    )

extension (f: Form.Fields)
  def |>(field: Field[?]): Form.Fields =
    Form.Fields(f.prefix, f.config, f.fields :+ FormEntry.Input(field))
  def ||(sb: SelectBuilder): Form.Fields =
    Form.Fields(
      f.prefix,
      f.config,
      f.fields :+ FormEntry.Select(SelectField(sb.label, sb.options))
    )
  def |*(sb: SelectBuilder): Form.Fields =
    Form.Fields(
      f.prefix,
      f.config,
      f.fields :+ FormEntry.Checkbox(CheckboxGroup(sb.label, sb.options))
    )

  /** Append radio button group */
  def |@(rb: RadioBuilder): Form.Fields =
    Form.Fields(
      f.prefix,
      f.config,
      f.fields :+ FormEntry.Radio(RadioGroup(rb.label, rb.options))
    )
  def |>>(legend: I18n[?]): Form.InFieldset =
    Form.InFieldset(f.prefix, f.config, f.fields, legend, Vector.empty)
  def |!(submitLabel: I18n[?]): Form.Done =
    Form.Done(f.prefix, f.config, f.fields, submitLabel)

extension (fs: Form.InFieldset)
  def |>(field: Field[?]): Form.InFieldset =
    Form.InFieldset(
      fs.prefix,
      fs.config,
      fs.outer,
      fs.legend,
      fs.inner :+ FormEntry.Input(field)
    )
  def ||(sb: SelectBuilder): Form.InFieldset =
    Form.InFieldset(
      fs.prefix,
      fs.config,
      fs.outer,
      fs.legend,
      fs.inner :+ FormEntry.Select(SelectField(sb.label, sb.options))
    )
  def |*(sb: SelectBuilder): Form.InFieldset =
    Form.InFieldset(
      fs.prefix,
      fs.config,
      fs.outer,
      fs.legend,
      fs.inner :+ FormEntry.Checkbox(CheckboxGroup(sb.label, sb.options))
    )

  /** Add radio button group inside fieldset */
  def |@(rb: RadioBuilder): Form.InFieldset =
    Form.InFieldset(
      fs.prefix,
      fs.config,
      fs.outer,
      fs.legend,
      fs.inner :+ FormEntry.Radio(RadioGroup(rb.label, rb.options))
    )
  def |<< : Form.Fields =
    val fieldset = FormEntry.Fieldset(fs.legend, fs.inner)
    Form.Fields(fs.prefix, fs.config, fs.outer :+ fieldset)

// ============================================================
// Form ADT
// ============================================================

enum Form[S <: FormState]:
  case Init(prefix: String, config: FormConfig = FormConfig())
      extends Form[FStart]
  case Fields(prefix: String, config: FormConfig, fields: Vector[FormEntry])
      extends Form[FFields]
  case InFieldset(
      prefix: String,
      config: FormConfig,
      outer: Vector[FormEntry],
      legend: I18n[?],
      inner: Vector[FormEntry]
  ) extends Form[FFieldset]
  case Done(
      prefix: String,
      config: FormConfig,
      fields: Vector[FormEntry],
      submitLabel: I18n[?]
  ) extends Form[FDone]

object Form:
  def apply(prefix: String): Form.Init = Form.Init(prefix)
