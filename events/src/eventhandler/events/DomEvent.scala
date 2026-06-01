package eventhandler.events

import org.scalajs.dom

/** Algebraic data type representing browser DOM events with extracted metadata.
  *
  * Each case carries:
  *   - The raw DOM event for access to native methods (preventDefault,
  *     stopPropagation, etc.)
  *   - An optional targetId for element identification (extracted from id
  *     attribute)
  *   - Relevant extracted data (value, checked state, key codes, etc.)
  *
  * This ADT enables pattern matching on events by type and ID, supporting the
  * document-level event delegation pattern where all events bubble up to a
  * central handler.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.events.DomEvent
  *
  * eventStream.foreach {
  *   case DomEvent.Click(event, Some(id), target) if id == "submit-btn" =>
  *     event.preventDefault()
  *     handleSubmit()
  *
  *   case DomEvent.Change(_, Some(id), _, value) if id.startsWith("form-") =>
  *     updateField(id, value)
  *
  *   case DomEvent.KeyDown(_, _, key, _) if key == "Escape" =>
  *     closeModal()
  *
  *   case _ => ()
  * }
  * }}}
  */
enum DomEvent:

  /** Mouse click event.
    *
    * @param event
    *   Raw MouseEvent for access to clientX/Y, button, modifiers, etc.
    * @param targetId
    *   ID attribute of the clicked element, if present
    * @param target
    *   The DOM element that was clicked
    */
  case Click(
      event: dom.MouseEvent,
      targetId: Option[String],
      target: dom.Element
  )

  /** Generic change event for form elements.
    *
    * @param event
    *   Raw Event
    * @param targetId
    *   ID attribute of the changed element, if present
    * @param target
    *   The DOM element that changed
    * @param value
    *   Current value of the element (from value property)
    */
  case Change(
      event: dom.Event,
      targetId: Option[String],
      target: dom.Element,
      value: String
  )

  /** Checkbox-specific change event with boolean state.
    *
    * @param event
    *   Raw Event
    * @param targetId
    *   ID attribute of the checkbox, if present
    * @param checked
    *   Current checked state
    */
  case CheckboxChange(
      event: dom.Event,
      targetId: Option[String],
      checked: Boolean
  )

  /** Select element change event with selection details.
    *
    * @param event
    *   Raw Event
    * @param targetId
    *   ID attribute of the select element, if present
    * @param selectedValue
    *   Value of the selected option
    * @param selectedIndex
    *   Index of the selected option
    */
  case SelectChange(
      event: dom.Event,
      targetId: Option[String],
      selectedValue: String,
      selectedIndex: Int
  )

  /** Form submission event.
    *
    * @param event
    *   Raw Event (call preventDefault to stop browser submission)
    * @param targetId
    *   ID attribute of the form, if present
    * @param target
    *   The form element
    */
  case Submit(event: dom.Event, targetId: Option[String], target: dom.html.Form)

  /** Input event fired during text entry.
    *
    * @param event
    *   Raw Event
    * @param targetId
    *   ID attribute of the input element, if present
    * @param value
    *   Current value of the input
    */
  case Input(event: dom.Event, targetId: Option[String], value: String)

  /** Key down event.
    *
    * @param event
    *   Raw KeyboardEvent for access to modifiers
    * @param targetId
    *   ID attribute of the focused element, if present
    * @param key
    *   The key value (e.g., "Enter", "Escape", "a")
    * @param code
    *   The physical key code (e.g., "KeyA", "Enter")
    */
  case KeyDown(
      event: dom.KeyboardEvent,
      targetId: Option[String],
      key: String,
      code: String
  )

  /** Key up event.
    *
    * @param event
    *   Raw KeyboardEvent for access to modifiers
    * @param targetId
    *   ID attribute of the focused element, if present
    * @param key
    *   The key value
    * @param code
    *   The physical key code
    */
  case KeyUp(
      event: dom.KeyboardEvent,
      targetId: Option[String],
      key: String,
      code: String
  )

  /** Focus gained event.
    *
    * @param event
    *   Raw FocusEvent
    * @param targetId
    *   ID attribute of the focused element, if present
    */
  case Focus(event: dom.FocusEvent, targetId: Option[String])

  /** Focus lost event.
    *
    * @param event
    *   Raw FocusEvent
    * @param targetId
    *   ID attribute of the blurred element, if present
    */
  case Blur(event: dom.FocusEvent, targetId: Option[String])

object DomEvent:

  /** Extract the target ID from any DomEvent. */
  extension (event: DomEvent)
    def targetId: Option[String] = event match
      case Click(_, id, _)           => id
      case Change(_, id, _, _)       => id
      case CheckboxChange(_, id, _)  => id
      case SelectChange(_, id, _, _) => id
      case Submit(_, id, _)          => id
      case Input(_, id, _)           => id
      case KeyDown(_, id, _, _)      => id
      case KeyUp(_, id, _, _)        => id
      case Focus(_, id)              => id
      case Blur(_, id)               => id

    /** Get the raw DOM event from any DomEvent variant. */
    def rawEvent: dom.Event = event match
      case Click(e, _, _)           => e
      case Change(e, _, _, _)       => e
      case CheckboxChange(e, _, _)  => e
      case SelectChange(e, _, _, _) => e
      case Submit(e, _, _)          => e
      case Input(e, _, _)           => e
      case KeyDown(e, _, _, _)      => e
      case KeyUp(e, _, _, _)        => e
      case Focus(e, _)              => e
      case Blur(e, _)               => e

    /** Prevent default browser behavior. */
    def preventDefault(): Unit = rawEvent.preventDefault()

    /** Stop event propagation. */
    def stopPropagation(): Unit = rawEvent.stopPropagation()
