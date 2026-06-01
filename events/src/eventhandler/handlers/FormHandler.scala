package eventhandler.handlers

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import htmlid.HtmlId
import eventhandler.events.{DomEvent, DomEventSource, EventFilter}
import org.scalajs.dom

/** Form event handler with typed field access.
  *
  * Provides a convenient way to handle form submissions and field changes for a
  * specific form identified by its type-safe HtmlId.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.handlers.FormHandler
  * import htmlid.{HtmlId, IdScope}
  *
  * object ids:
  *   object loginForm extends IdScope["login-form"]:
  *     val email = id["email"]
  *     val password = id["password"]
  *   val loginFormId: HtmlId["login-form"] = HtmlId["login-form"]
  *
  * given owner: Owner = new ManualOwner
  *
  * val formHandler = FormHandler(ids.loginFormId)
  *
  * // Handle form submission
  * formHandler.onSubmit { submit =>
  *   submit.preventDefault()
  *   val values = extractFormValues(submit.target)
  *   attemptLogin(values)
  * }
  *
  * // Watch individual field changes
  * formHandler.onChange(ids.loginForm.email) { change =>
  *   validateEmail(change.value)
  * }
  *
  * // Track all form values as a signal
  * formHandler.formValues.observe { values =>
  *   updateSubmitButtonState(values)
  * }
  * }}}
  *
  * @tparam P
  *   The form ID type (singleton string literal)
  */
class FormHandler[P <: String](
    formId: HtmlId[P],
    target: dom.EventTarget = dom.document,
    useCapture: Boolean = true
)(using owner: Owner):

  import EventFilter.*

  /** Stream of submit events for this form. */
  val submits: EventStream[DomEvent.Submit] =
    DomEventSource.submits(target, useCapture).forId(formId)

  /** Stream of all change events on elements within this form. */
  val changes: EventStream[DomEvent.Change] =
    DomEventSource.changes(target, useCapture)

  /** Stream of all input events on elements within this form. */
  val inputs: EventStream[DomEvent.Input] =
    DomEventSource.inputs(target, useCapture)

  /** Internal state for tracking form values. */
  private val valuesVar: Var[Map[String, String]] = Var(Map.empty)

  // Track changes to update values
  private val _changeSub = changes.foreach { change =>
    change.targetId.foreach { id =>
      valuesVar.update(_.updated(id, change.value))
    }
  }

  private val _inputSub = inputs.foreach { input =>
    input.targetId.foreach { id =>
      valuesVar.update(_.updated(id, input.value))
    }
  }

  /** Register a handler for form submission.
    *
    * @param handler
    *   Callback function receiving the submit event
    */
  def onSubmit(handler: DomEvent.Submit => Unit): Unit =
    val _ = submits.foreach(handler)

  /** Register a handler for changes on a specific field.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @param handler
    *   Callback function receiving the change event
    */
  def onChange[S <: String](fieldId: HtmlId[S])(
      handler: DomEvent.Change => Unit
  ): Unit =
    val _ = changes.forId(fieldId).foreach(handler)

  /** Register a handler for input events on a specific field.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @param handler
    *   Callback function receiving the input event
    */
  def onInput[S <: String](fieldId: HtmlId[S])(
      handler: DomEvent.Input => Unit
  ): Unit =
    val _ = inputs.forId(fieldId).foreach(handler)

  /** Get the stream of changes for a specific field.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @return
    *   Stream of change events for the field
    */
  def changesFor[S <: String](
      fieldId: HtmlId[S]
  ): EventStream[DomEvent.Change] =
    changes.forId(fieldId)

  /** Get the stream of input events for a specific field.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @return
    *   Stream of input events for the field
    */
  def inputsFor[S <: String](fieldId: HtmlId[S]): EventStream[DomEvent.Input] =
    inputs.forId(fieldId)

  /** Signal of current form values (field ID -> value).
    *
    * This is a KTable-like accumulation of all field changes. Useful for form
    * validation and submit button state.
    */
  def formValues: Signal[Map[String, String]] = valuesVar.signal

  /** Get the current value of a specific field.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @return
    *   Current value or empty string if not yet changed
    */
  def valueOf[S <: String](fieldId: HtmlId[S]): String =
    valuesVar.now().getOrElse(fieldId.value, "")

  /** Signal of a specific field's value.
    *
    * @param fieldId
    *   The type-safe HtmlId of the field
    * @return
    *   Signal of the field value
    */
  def signalFor[S <: String](fieldId: HtmlId[S]): Signal[String] =
    valuesVar.signal.map(_.getOrElse(fieldId.value, ""))

  /** Reset tracked form values. */
  def reset(): Unit =
    valuesVar.set(Map.empty)

object FormHandler:

  /** Create a FormHandler for a specific form.
    *
    * @param formId
    *   The type-safe HtmlId of the form
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def apply[P <: String](
      formId: HtmlId[P],
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using Owner): FormHandler[P] =
    new FormHandler(formId, target, useCapture)
