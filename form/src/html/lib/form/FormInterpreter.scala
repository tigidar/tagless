package html.lib.form

import tags.{Node, Attr}
import tags.i18n.{I18n, Lang}
import scala.language.implicitConversions

/** Interpreter that converts Form DSL to Node for integration with the HTML
  * DSL.
  *
  * Only complete forms (FDone) can be converted. This is enforced at compile
  * time via the type constraint.
  *
  * Usage:
  * {{{
  * import html.lib.form.*
  * import html.lib.form.FormInterpreter.given
  * import tags.dsl.*
  *
  * val form = Form("user")
  *   |> "Name".txt.required
  *   |> "Email".email
  *   |! "Submit"
  *
  * div.toCursor >>^ form  // Auto-converts to Node
  * }}}
  */
object FormInterpreter:

  private given CanEqual[FieldType, FieldType] = CanEqual.derived

  /** Convert a complete form to a Node.Element */
  def toNode(form: Form[FDone])(using Lang): Node.Element =
    form match
      case d: Form.Done =>
        buildForm(d.prefix, d.config, d.fields, d.submitLabel)

  /** Implicit conversion for FDone forms */
  given formDoneToNode(using Lang): Conversion[Form[FDone], Node] = toNode(_)

  // ============================================================
  // Private builder methods
  // ============================================================

  private def buildForm(
      prefix: String,
      config: FormConfig,
      fields: Vector[FormEntry],
      submitLabel: I18n[?]
  )(using lang: Lang): Node.Element =
    val children = Vector.newBuilder[Node]
    fields.foreach(entry => children += buildEntry(prefix, entry))
    children += Node.Element(
      "button",
      List(Attr.Type("submit")),
      Vector(Node.Text(submitLabel.apply))
    )
    val formAttrs = List.newBuilder[Attr]
    formAttrs += Attr.Id(s"$prefix-form")
    config.action.foreach(v => formAttrs += Attr.Action(v))
    config.method.foreach(v => formAttrs += Attr.Method(v))
    config.enctype.foreach(v => formAttrs += Attr.Enctype(v))
    if config.novalidate then formAttrs += Attr.Novalidate
    Node.Element("form", formAttrs.result(), children.result())

  private def buildEntry(prefix: String, entry: FormEntry)(using Lang): Node =
    entry match
      case FormEntry.Input(field) => buildInputField(prefix, field)
      case FormEntry.Select(sf)   => buildSelectField(prefix, sf)
      case FormEntry.Checkbox(cg) => buildCheckboxGroup(prefix, cg)
      case FormEntry.Radio(rg)    => buildRadioGroup(prefix, rg)
      case FormEntry.Fieldset(legend, entries) =>
        buildFieldset(prefix, legend, entries)

  private def buildInputField(prefix: String, field: Field[?])(using
      lang: Lang
  ): Node.Element =
    val labelText = field.label.apply
    val fieldId = s"$prefix-${slugify(labelText)}"
    val labelEl = Node.Element(
      "label",
      List(Attr.For(fieldId)),
      Vector(Node.Text(labelText))
    )

    val inputEl = field.fieldType match
      case FieldType.Area =>
        val attrs =
          List(Attr.Id(fieldId), Attr.Name(fieldId)) ++ fieldAttrsToList(
            field.attrs
          )
        Node.Element("textarea", attrs, Vector.empty)
      case ft =>
        val typeStr = fieldTypeToString(ft)
        val attrs = List(
          Attr.Id(fieldId),
          Attr.Name(fieldId),
          Attr.Type(typeStr)
        ) ++ fieldAttrsToList(field.attrs)
        Node.VoidElement("input", attrs)

    Node.Element("div", Nil, Vector(labelEl, inputEl))

  private def buildSelectField(prefix: String, sf: SelectField)(using
      lang: Lang
  ): Node.Element =
    val labelText = sf.label.apply
    val fieldId = s"$prefix-${slugify(labelText)}"
    val labelEl = Node.Element(
      "label",
      List(Attr.For(fieldId)),
      Vector(Node.Text(labelText))
    )
    val options = sf.options.map { opt =>
      val optText = opt.apply
      Node.Element(
        "option",
        List(Attr.Value(optText)),
        Vector(Node.Text(optText))
      )
    }
    val selectEl = Node.Element(
      "select",
      List(Attr.Id(fieldId), Attr.Name(fieldId)),
      options
    )
    Node.Element("div", Nil, Vector(labelEl, selectEl))

  private def buildCheckboxGroup(prefix: String, cg: CheckboxGroup)(using
      lang: Lang
  ): Node.Element =
    val labelText = cg.label.apply
    val legendEl = Node.Element("legend", Nil, Vector(Node.Text(labelText)))
    val checkboxes = cg.options.flatMap { opt =>
      val optText = opt.apply
      val cbId = s"$prefix-${slugify(labelText)}-${slugify(optText)}"
      val inputEl = Node.VoidElement(
        "input",
        List(
          Attr.Type("checkbox"),
          Attr.Id(cbId),
          Attr.Name(s"$prefix-${slugify(labelText)}"),
          Attr.Value(optText)
        )
      )
      val labelEl = Node.Element(
        "label",
        List(Attr.For(cbId)),
        Vector(Node.Text(optText))
      )
      Vector(inputEl, labelEl)
    }
    Node.Element("fieldset", Nil, Vector(legendEl) ++ checkboxes)

  private def buildRadioGroup(prefix: String, rg: RadioGroup)(using
      lang: Lang
  ): Node.Element =
    val labelText = rg.label.apply
    val baseName = s"$prefix-${slugify(labelText)}"

    val legendEl = Node.Element("legend", Nil, Vector(Node.Text(labelText)))

    val radios = rg.options.flatMap { opt =>
      val optText = opt.apply
      val radioId = s"$baseName-${slugify(optText)}"

      val inputEl = Node.VoidElement(
        "input",
        List(
          Attr.Type("radio"),
          Attr.Id(radioId),
          Attr.Name(baseName), // Same name for mutual exclusion
          Attr.Value(optText)
        )
      )

      val labelEl = Node.Element(
        "label",
        List(Attr.For(radioId)),
        Vector(Node.Text(optText))
      )

      Vector(inputEl, labelEl)
    }

    Node.Element("fieldset", Nil, Vector(legendEl) ++ radios)

  private def buildFieldset(
      prefix: String,
      legend: I18n[?],
      entries: Vector[FormEntry]
  )(using lang: Lang): Node.Element =
    val legendEl = Node.Element("legend", Nil, Vector(Node.Text(legend.apply)))
    val children = entries.map(e => buildEntry(prefix, e))
    Node.Element("fieldset", Nil, Vector(legendEl) ++ children)

  private def slugify(s: String): String =
    s.toLowerCase
      .replaceAll("[^a-z0-9]+", "-")
      .stripPrefix("-")
      .stripSuffix("-")

  private def fieldTypeToString(ft: FieldType): String = ft match
    case FieldType.Text     => "text"
    case FieldType.Area     => "textarea"
    case FieldType.Number   => "number"
    case FieldType.Email    => "email"
    case FieldType.Password => "password"
    case FieldType.Hidden   => "hidden"
    case FieldType.Tel      => "tel"
    case FieldType.Url      => "url"
    case FieldType.Search   => "search"
    case FieldType.Date     => "date"
    case FieldType.Time     => "time"
    case FieldType.Color    => "color"
    case FieldType.Range    => "range"
    case FieldType.File     => "file"

  private def fieldAttrsToList(attrs: FieldAttrs): List[Attr] =
    val b = List.newBuilder[Attr]
    if attrs.required then b += Attr.Required
    attrs.pattern.foreach(p => b += Attr.Custom("pattern", p))
    attrs.min.foreach(v => b += Attr.Custom("min", v))
    attrs.max.foreach(v => b += Attr.Custom("max", v))
    attrs.placeholder.foreach(p => b += Attr.Placeholder(p))
    b.result()
