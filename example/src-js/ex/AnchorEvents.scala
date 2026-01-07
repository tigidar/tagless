package ex

import com.raquo.airstream.core.{EventStream, Signal, Observer}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.{Owner, ManualOwner}
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLAnchorElement, PopStateEvent}
import org.scalajs.dom.html
import org.scalajs.dom.document
import org.scalajs.dom.Element
import org.scalajs.dom.window
import com.raquo.airstream.state.Var

object MouseEvents:
  val onClick = "onclick"
  val onMouseOver = "onmouseover"
  val onMouseOut = "onmouseout"
  val onFocus = "onfocus"
  val onBlur = "onblur"

/** URL routing configuration for the app.
  * Maps URL paths to button IDs used by the existing PageMap.
  */
object UrlRoutes:
  /** Map of URL path -> button ID */
  val pathToButtonId: Map[String, String] = Map(
    "/" -> PageMap.NavButtons.homeId,
    "/todos" -> PageMap.NavButtons.todoId,
    "/contact" -> PageMap.NavButtons.contactId,
    "/about" -> PageMap.NavButtons.aboutId
  )

  /** Map of button ID -> URL path */
  val buttonIdToPath: Map[String, String] =
    pathToButtonId.map { case (k, v) => (v, k) }

  /** Get button ID for a URL path */
  def buttonIdForPath(path: String): Option[String] =
    val normalized = if path.isEmpty || path == "/" then "/" else path.stripSuffix("/")
    pathToButtonId.get(normalized)

  /** Get URL path for a button ID */
  def pathForButtonId(buttonId: String): Option[String] =
    buttonIdToPath.get(buttonId)

/** Handles anchor/navigation events using Airstream signals.
  *
  * This class demonstrates the typed state pattern with URL routing:
  * 1. Events come in as typed Anchor elements or from browser history
  * 2. State transitions are calculated using the typed PageMap routes
  * 3. DOM side effects are isolated in the Page.handleNavigation method
  * 4. The signal maintains the full typed Page state
  * 5. Browser URL is synchronized with navigation state
  */
final case class AnchorEvents(enableUrlRouting: Boolean = true)(using o: Owner):

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
        val newPage = currentPage.handleNavigation(anchor.id)

        // Update browser URL if routing is enabled
        if enableUrlRouting then
          UrlRoutes.pathForButtonId(anchor.id).foreach { path =>
            if window.location.pathname != path then
              window.history.pushState(null, "", path)
              println(s"URL updated to: $path")
          }

        newPage
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

  // Set up browser history listener for back/forward navigation
  if enableUrlRouting then
    val popstateListener: scala.scalajs.js.Function1[PopStateEvent, Unit] = { _ =>
      val path = window.location.pathname
      println(s"Popstate event: navigating to $path")
      UrlRoutes.buttonIdForPath(path).foreach { buttonId =>
        // Find or create anchor for this button
        Option(document.getElementById(buttonId)).foreach {
          case anchor: html.Anchor => submit(anchor)
          case _ => println(s"Warning: $buttonId is not an anchor element")
        }
      }
    }
    window.addEventListener("popstate", popstateListener)

  /** Submit an anchor click event to the event bus */
  def submit(a: html.Anchor): Unit =
    println(s"Navigation requested: ${a.id} -> ${a.href}")
    EventBus.emit(pageEventBus -> a)

  /** Navigate to a specific URL path programmatically */
  def navigateToPath(path: String): Unit =
    UrlRoutes.buttonIdForPath(path).foreach { buttonId =>
      Option(document.getElementById(buttonId)).foreach {
        case anchor: html.Anchor => submit(anchor)
        case _ => println(s"Warning: $buttonId is not an anchor element")
      }
    }

  /** Navigate using the initial URL when the app loads */
  def handleInitialUrl(): Unit =
    if enableUrlRouting then
      val path = window.location.pathname
      if path != "/" then
        println(s"Handling initial URL: $path")
        navigateToPath(path)

  /** Check if a specific page is currently active by element ID */
  def isPageActive(elementId: String)(using page: Page): Boolean =
    page.switchState.visibility.currentElementId.contains(elementId)
