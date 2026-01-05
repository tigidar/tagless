package ex

import com.raquo.airstream.core.{EventStream, Signal, Observer}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.{Owner, ManualOwner}
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLAnchorElement}
import org.scalajs.dom.html
import org.scalajs.dom.document
import org.scalajs.dom.Element
import com.raquo.airstream.state.Var

object MouseEvents:
  val onClick = "onclick"
  val onMouseOver = "onmouseover"
  val onMouseOut = "onmouseout"
  val onFocus = "onfocus"
  val onBlur = "onblur"


/** Handles anchor/navigation events using Airstream signals.
  *
  * This class demonstrates the typed state pattern:
  * 1. Events come in as typed Anchor elements
  * 2. State transitions are calculated using the typed PageMap routes
  * 3. DOM side effects are isolated in the Page.handleNavigation method
  * 4. The signal maintains the full typed Page state
  */
final case class AnchorEvents()(using o: Owner):

  /** Event bus for incoming anchor click events */
  val pageEventBus = new EventBus[html.Anchor]

  /** Signal that accumulates page navigation state.
    * Uses scanLeft to maintain state across events.
    * The Page type contains:
    * - switchState: current visibility and page definition
    * - pageState: optional typed state for stateful pages (TodoList, ErrorMessage, etc.)
    */
  val pageStateSignal: Signal[Page] =
    pageEventBus.events
      .scanLeft(Page.initial) { (currentPage, anchor) =>
        println(s"AnchorEvents: processing navigation to ${anchor.id}")
        currentPage.handleNavigation(anchor.id)
      }

  /** Observer that activates the signal and logs state changes.
    * Signals in Airstream are lazy - they need at least one observer to activate.
    * This also provides a hook for browser history integration.
    */
  val pageObserver: Observer[Page] = Observer[Page] { page =>
    println(s"Current page: ${page.switchState.currentPage.elementId}")
    page.pageState.foreach { state =>
      state match
        case PageState.TodoList(items) =>
          println(s"  TodoList with ${items.length} items")
        case PageState.ErrorMessage(msg) =>
          println(s"  Error: $msg")
        case PageState.NoState =>
          () // No state to report
    }
  }

  // Activate the signal with our observer
  val subscription = pageStateSignal.addObserver(pageObserver)

  /** Submit an anchor click event to the event bus */
  def submit(a: html.Anchor): Unit =
    println(s"Navigation requested: ${a.id} -> ${a.href}")
    EventBus.emit(pageEventBus -> a)

  /** Check if a specific page is currently active by element ID */
  def isPageActive(elementId: String)(using page: Page): Boolean =
    page.switchState.visibility.currentElementId == elementId
