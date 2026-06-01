package eventhandler.handlers

import eventhandler.events.{DomEventSource, DomEvent, EventFilter}
import EventFilter.*
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var
import com.raquo.airstream.ownership.Owner
import org.scalajs.dom

/** CRUD mode state for view/edit toggling. */
enum CrudMode derives CanEqual:
  case View, Edit, Create

/** Handler for CRUD view/edit mode toggling with reactive streams.
  *
  * Manages mode state and provides filtered event streams for standard CRUD
  * buttons. Automatically toggles DOM visibility based on mode changes.
  *
  * ==ID Conventions==
  *
  * | Element        | ID Pattern            | Example           |
  * |:---------------|:----------------------|:------------------|
  * | View container | `{prefix}-view`       | `todo-view`       |
  * | Edit container | `{prefix}-edit`       | `todo-edit`       |
  * | Edit button    | `btn-{prefix}-edit`   | `btn-todo-edit`   |
  * | Cancel button  | `btn-{prefix}-cancel` | `btn-todo-cancel` |
  * | Save button    | `btn-{prefix}-save`   | `btn-todo-save`   |
  * | Delete button  | `btn-{prefix}-delete` | `btn-todo-delete` |
  * | Form           | `{prefix}-form`       | `todo-form`       |
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.handlers.{CrudHandler, CrudMode}
  * import com.raquo.airstream.ownership.ManualOwner
  *
  * given owner: ManualOwner = new ManualOwner
  *
  * val handler = CrudHandler("todo")
  *
  * // Auto-wired: edit button → Edit mode, cancel button → View mode
  *
  * // Handle save clicks
  * handler.saveClicks.foreach { _ =>
  *   val formData = collectFormData()
  *   saveItem(formData)
  *   handler.exitToView()
  * }
  *
  * // Handle delete clicks
  * handler.deleteClicks.foreach { _ =>
  *   if confirmDelete() then
  *     deleteItem()
  *     handler.exitToView()
  * }
  *
  * // React to mode changes
  * handler.mode.foreach {
  *   case CrudMode.View => println("Viewing")
  *   case CrudMode.Edit => println("Editing")
  *   case CrudMode.Create => println("Creating")
  * }
  * }}}
  *
  * @param resourcePrefix
  *   The resource identifier prefix (e.g., "todo", "user")
  * @param target
  *   DOM element to listen on (default: document)
  */
class CrudHandler(
    resourcePrefix: String,
    target: dom.EventTarget = dom.document
)(using owner: Owner):

  private val modeVar: Var[CrudMode] = Var(CrudMode.View)

  /** Signal of current CRUD mode. */
  val mode: Signal[CrudMode] = modeVar.signal

  // Button click streams using ID matching
  private val clicks = DomEventSource.clicks(target)

  /** Stream of edit button clicks. */
  val editClicks: EventStream[DomEvent.Click] =
    clicks.filterById(_ == s"btn-$resourcePrefix-edit")

  /** Stream of cancel button clicks. */
  val cancelClicks: EventStream[DomEvent.Click] =
    clicks.filterById(_ == s"btn-$resourcePrefix-cancel")

  /** Stream of save button clicks. */
  val saveClicks: EventStream[DomEvent.Click] =
    clicks.filterById(_ == s"btn-$resourcePrefix-save")

  /** Stream of delete button clicks. */
  val deleteClicks: EventStream[DomEvent.Click] =
    clicks.filterById(_ == s"btn-$resourcePrefix-delete")

  /** Stream of create button clicks. */
  val createClicks: EventStream[DomEvent.Click] =
    clicks.filterById(_ == s"btn-$resourcePrefix-create")

  /** Stream of form submissions. */
  val formSubmits: EventStream[DomEvent.Submit] =
    DomEventSource.submits(target).filterById(_ == s"$resourcePrefix-form")

  /** Transition to Edit mode. */
  def enterEdit(): Unit = modeVar.set(CrudMode.Edit)

  /** Transition to Create mode. */
  def enterCreate(): Unit = modeVar.set(CrudMode.Create)

  /** Transition to View mode. */
  def exitToView(): Unit = modeVar.set(CrudMode.View)

  /** Get the current mode. */
  def currentMode: CrudMode = modeVar.now()

  // Auto-wire standard transitions
  private val _editSub = editClicks.foreach(_ => enterEdit())
  private val _cancelSub = cancelClicks.foreach(_ => exitToView())
  private val _createSub = createClicks.foreach(_ => enterCreate())

  // DOM visibility toggling
  private def syncDom(): Unit =
    val viewEl = Option(dom.document.getElementById(s"$resourcePrefix-view"))
    val editEl = Option(dom.document.getElementById(s"$resourcePrefix-edit"))
    modeVar.now() match
      case CrudMode.View =>
        viewEl.foreach(_.classList.remove("hidden"))
        editEl.foreach(_.classList.add("hidden"))
      case CrudMode.Edit | CrudMode.Create =>
        viewEl.foreach(_.classList.add("hidden"))
        editEl.foreach(_.classList.remove("hidden"))

  // Subscribe to mode changes for DOM sync
  private val _modeSub = mode.foreach(_ => syncDom())

object CrudHandler:

  /** Create a CrudHandler for a specific resource.
    *
    * @param resourcePrefix
    *   The resource identifier prefix (e.g., "todo", "user")
    * @param target
    *   DOM element to listen on (default: document)
    */
  def apply(resourcePrefix: String, target: dom.EventTarget = dom.document)(
      using Owner
  ): CrudHandler =
    new CrudHandler(resourcePrefix, target)
