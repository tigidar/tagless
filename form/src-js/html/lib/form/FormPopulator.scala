package html.lib.form

import org.scalajs.dom
import tags.i18n.Lang

/** Client-side form population utilities for ScalaJS.
  *
  * Provides methods to populate form fields with values from case class
  * instances using the FormDerivation typeclass.
  *
  * ==Usage==
  *
  * {{{
  * import html.lib.form.*
  *
  * case class Todo(title: String, description: String)
  *
  * // Given a FormDerivation instance
  * given FormDerivation[Todo] = FormDerivation.withLabels[Todo](...)
  *
  * val todo = Todo("Buy groceries", "Get milk and bread")
  *
  * // Populate form fields
  * FormPopulator.populate(todo, "todo")
  * }}}
  */
object FormPopulator:

  /** Populate form fields with values from a case class instance.
    *
    * Field IDs are derived using the same slugify algorithm as FormInterpreter:
    * `$prefix-${slugify(labelText)}`
    *
    * @param item
    *   The case class instance containing values
    * @param prefix
    *   The form prefix used when generating the form
    * @param lang
    *   Language for resolving label text (needed for field ID generation)
    */
  def populate[A](item: A, prefix: String)(using
      fd: FormDerivation[A],
      lang: Lang
  ): Unit =
    val values = fd.extractValues(item)
    fd.fields.foreach { field =>
      val labelText = field.label.apply
      val fieldId = s"$prefix-${slugify(labelText)}"
      values.get(field.name).foreach { value =>
        setFieldValue(fieldId, value)
      }
    }

  /** Populate form fields using a map of field names to values.
    *
    * @param values
    *   Map of field name to string value
    * @param prefix
    *   The form prefix
    * @param lang
    *   Language for resolving label text
    */
  def populateFromMap[A](values: Map[String, String], prefix: String)(using
      fd: FormDerivation[A],
      lang: Lang
  ): Unit =
    fd.fields.foreach { field =>
      val labelText = field.label.apply
      val fieldId = s"$prefix-${slugify(labelText)}"
      values.get(field.name).foreach { value =>
        setFieldValue(fieldId, value)
      }
    }

  /** Clear all form fields to their default/empty values.
    *
    * @param prefix
    *   The form prefix
    * @param lang
    *   Language for resolving label text
    */
  def clear[A](prefix: String)(using fd: FormDerivation[A], lang: Lang): Unit =
    fd.fields.foreach { field =>
      val labelText = field.label.apply
      val fieldId = s"$prefix-${slugify(labelText)}"
      setFieldValue(fieldId, "")
    }

  /** Collect form field values into a map.
    *
    * @param prefix
    *   The form prefix
    * @param lang
    *   Language for resolving label text
    * @return
    *   Map of field name to current value
    */
  def collect[A](prefix: String)(using
      fd: FormDerivation[A],
      lang: Lang
  ): Map[String, String] =
    fd.fields.flatMap { field =>
      val labelText = field.label.apply
      val fieldId = s"$prefix-${slugify(labelText)}"
      getFieldValue(fieldId).map(field.name -> _)
    }.toMap

  // Private helpers

  private def setFieldValue(fieldId: String, value: String): Unit =
    Option(dom.document.getElementById(fieldId)).foreach {
      case input: dom.html.Input       => input.value = value
      case textarea: dom.html.TextArea => textarea.value = value
      case select: dom.html.Select     => select.value = value
      case _                           => ()
    }

  private def getFieldValue(fieldId: String): Option[String] =
    Option(dom.document.getElementById(fieldId)).flatMap {
      case input: dom.html.Input       => Some(input.value)
      case textarea: dom.html.TextArea => Some(textarea.value)
      case select: dom.html.Select     => Some(select.value)
      case _                           => None
    }

  private def slugify(s: String): String =
    s.toLowerCase
      .replaceAll("[^a-z0-9]+", "-")
      .stripPrefix("-")
      .stripSuffix("-")
