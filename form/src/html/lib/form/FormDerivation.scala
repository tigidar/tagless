package html.lib.form

import scala.deriving.Mirror
import scala.compiletime.*
import tags.i18n.{I18n, Lang}

/** Metadata for a single form field derived from a case class. */
case class DerivedField(
    name: String,
    label: I18n[?],
    fieldType: FieldType,
    required: Boolean
)

/** Typeclass for deriving forms from case classes.
  *
  * Provides automatic form generation from case class definitions. Field types
  * are mapped to appropriate HTML input types.
  *
  * ==Usage==
  *
  * {{{
  * import html.lib.form.*
  * import tags.i18n.*
  *
  * case class Todo(
  *   title: String,
  *   description: String,
  *   dueDate: Option[java.time.LocalDate],
  *   priority: Int
  * )
  *
  * // Create derivation with explicit labels
  * given FormDerivation[Todo] = FormDerivation.create(
  *   DerivedField("title", TodoLabels.title, FieldType.Text, required = true),
  *   DerivedField("description", TodoLabels.description, FieldType.Area, required = false),
  *   DerivedField("dueDate", TodoLabels.dueDate, FieldType.Date, required = false),
  *   DerivedField("priority", TodoLabels.priority, FieldType.Number, required = true)
  * )(_.productIterator.toList)
  *
  * // Generate form
  * val fd = summon[FormDerivation[Todo]]
  * val form = fd.toForm("todo", submitLabel)
  * }}}
  */
trait FormDerivation[A]:

  /** Field metadata for this type. */
  def fields: Vector[DerivedField]

  /** Convert to a complete Form.Done that can be rendered.
    *
    * @param prefix
    *   The form ID prefix
    * @param submitLabel
    *   Label for the submit button
    */
  def toForm(prefix: String, submitLabel: I18n[?])(using Lang): Form.Done

  /** Extract field values from an instance as a map of field name to string
    * value.
    */
  def extractValues(item: A): Map[String, String]

object FormDerivation:

  private given CanEqual[None.type, Any] = CanEqual.derived
  private given CanEqual[Some[?], Any] = CanEqual.derived

  /** Create a FormDerivation from explicit field definitions.
    *
    * @param derivedFields
    *   The field metadata for each case class field
    * @param extractor
    *   Function to extract values from an instance (typically
    *   _.productIterator.toList)
    */
  def create[A](derivedFields: DerivedField*)(
      extractor: A => List[Any]
  ): FormDerivation[A] =
    val fieldsVec = derivedFields.toVector
    new FormDerivation[A]:
      def fields: Vector[DerivedField] = fieldsVec

      def toForm(prefix: String, submitLabel: I18n[?])(using Lang): Form.Done =
        val entries = fields.map { df =>
          val baseField = Field(df.label, df.fieldType)
          val field =
            if df.required && df.fieldType.isInstanceOf[CanBeRequired] then
              baseField.asInstanceOf[Field[FieldType & CanBeRequired]].required
            else baseField
          FormEntry.Input(field)
        }
        Form.Done(prefix, FormConfig(), entries, submitLabel)

      def extractValues(item: A): Map[String, String] =
        val values = extractor(item)
        fields
          .map(_.name)
          .zip(values)
          .map { case (name, value) =>
            name -> valueToString(value)
          }
          .toMap

  /** Create a FormDerivation for a Product type (case class).
    *
    * Uses reflection via productIterator for value extraction.
    */
  def forProduct[A <: Product](
      derivedFields: DerivedField*
  ): FormDerivation[A] =
    create[A](derivedFields*)(_.productIterator.toList)

  private def valueToString(value: Any): String =
    value match
      case None                        => ""
      case Some(v)                     => valueToString(v)
      case d: java.time.LocalDate      => d.toString
      case t: java.time.LocalTime      => t.toString
      case dt: java.time.LocalDateTime => dt.toString
      case other                       => other.toString
