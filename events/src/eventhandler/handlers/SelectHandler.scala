package eventhandler.handlers

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import htmlid.HtmlId
import eventhandler.events.{DomEvent, DomEventSource, EventFilter}
import org.scalajs.dom

/** Handler for select, checkbox, and radio input state tracking.
  *
  * Provides reactive signals and streams for tracking the selection state of
  * form inputs identified by their type-safe HtmlId.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.handlers.SelectHandler
  * import htmlid.HtmlId
  *
  * object ids:
  *   val themeSelect: HtmlId["theme-select"] = HtmlId["theme-select"]
  *   val darkModeCheckbox: HtmlId["dark-mode"] = HtmlId["dark-mode"]
  *
  * given owner: Owner = new ManualOwner
  *
  * // Select element
  * val themeHandler = SelectHandler.select(ids.themeSelect)
  * themeHandler.selectedValue.observe { theme =>
  *   applyTheme(theme)
  * }
  *
  * // Checkbox
  * val darkModeHandler = SelectHandler.checkbox(ids.darkModeCheckbox)
  * darkModeHandler.checked.observe { isDark =>
  *   toggleDarkMode(isDark)
  * }
  *
  * // React to changes
  * themeHandler.selectionChanges.foreach { value =>
  *   logThemeChange(value)
  * }
  * }}}
  *
  * @tparam S
  *   The element ID type (singleton string literal)
  */
class SelectHandler[S <: String] private (
    selectId: HtmlId[S],
    initialValue: String,
    target: dom.EventTarget,
    useCapture: Boolean
)(using owner: Owner):

  import EventFilter.*

  private val valueVar: Var[String] = Var(initialValue)

  /** Stream of change events for this select element. */
  val selectionChanges: EventStream[String] =
    DomEventSource
      .selectChanges(target, useCapture)
      .forId(selectId)
      .map(_.selectedValue)

  // Start listening to populate signal
  private val _sub = selectionChanges.foreach(v => valueVar.set(v))

  /** Signal of the currently selected value. */
  def selectedValue: Signal[String] = valueVar.signal

  /** Get the current selected value synchronously. */
  def currentValue: String = valueVar.now()

  /** Stream of selection index changes. */
  val indexChanges: EventStream[Int] =
    DomEventSource
      .selectChanges(target, useCapture)
      .forId(selectId)
      .map(_.selectedIndex)

object SelectHandler:

  /** Create a SelectHandler for a select element.
    *
    * @param selectId
    *   The type-safe HtmlId of the select element
    * @param initialValue
    *   Initial value before any selection (default: "")
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def select[S <: String](
      selectId: HtmlId[S],
      initialValue: String = "",
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using Owner): SelectHandler[S] =
    new SelectHandler(selectId, initialValue, target, useCapture)

  /** Create a handler for a checkbox element.
    *
    * @param checkboxId
    *   The type-safe HtmlId of the checkbox
    * @param initialState
    *   Initial checked state (default: false)
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def checkbox[S <: String](
      checkboxId: HtmlId[S],
      initialState: Boolean = false,
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using owner: Owner): CheckboxHandler[S] =
    new CheckboxHandler(checkboxId, initialState, target, useCapture)

  /** Create a handler for a radio button group.
    *
    * @param name
    *   The name attribute of the radio group
    * @param initialValue
    *   Initial selected value (default: "")
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def radioGroup(
      name: String,
      initialValue: String = "",
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using owner: Owner): RadioGroupHandler =
    new RadioGroupHandler(name, initialValue, target, useCapture)

/** Handler for checkbox state tracking.
  *
  * @tparam S
  *   The checkbox ID type (singleton string literal)
  */
class CheckboxHandler[S <: String](
    checkboxId: HtmlId[S],
    initialState: Boolean,
    target: dom.EventTarget,
    useCapture: Boolean
)(using owner: Owner):

  import EventFilter.*

  private val checkedVar: Var[Boolean] = Var(initialState)

  /** Stream of checked state changes. */
  val checkedChanges: EventStream[Boolean] =
    DomEventSource
      .checkboxChanges(target, useCapture)
      .forId(checkboxId)
      .map(_.checked)

  // Start listening to populate signal
  private val _sub = checkedChanges.foreach(v => checkedVar.set(v))

  /** Signal of the current checked state. */
  def checked: Signal[Boolean] = checkedVar.signal

  /** Get the current checked state synchronously. */
  def isChecked: Boolean = checkedVar.now()

  /** Toggle the checkbox state (for programmatic control). */
  def toggle(): Unit =
    checkedVar.update(!_)

/** Handler for radio button group state tracking. */
class RadioGroupHandler(
    name: String,
    initialValue: String,
    target: dom.EventTarget,
    useCapture: Boolean
)(using owner: Owner):

  private val selectedVar: Var[String] = Var(initialValue)

  /** Stream of selection changes in the radio group. */
  val selectionChanges: EventStream[String] =
    DomEventSource
      .changes(target, useCapture)
      .filter { change =>
        change.target match
          case input: dom.html.Input =>
            input.`type` == "radio" && input.name == name
          case _ => false
      }
      .map(_.value)

  // Start listening to populate signal
  private val _sub = selectionChanges.foreach(v => selectedVar.set(v))

  /** Signal of the currently selected value. */
  def selectedValue: Signal[String] = selectedVar.signal

  /** Get the current selected value synchronously. */
  def currentValue: String = selectedVar.now()
