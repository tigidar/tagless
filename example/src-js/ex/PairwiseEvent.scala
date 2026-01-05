package ex

import org.scalajs.dom.{HTMLElement, HTMLDivElement}
import org.scalajs.dom
import scala.util.Try
import org.scalajs.dom.document
import com.raquo.airstream.core.{Observer, EventStream, Signal}
import com.raquo.airstream.ownership.{Owner, ManualOwner}

// DOM helpers using type-safe CSS classes
inline def get[A <: HTMLElement](id: String) =
  dom.document.getElementById(id).asInstanceOf[A]

inline def getDiv(id: String): HTMLDivElement =
  dom.document.getElementById(id).asInstanceOf[HTMLDivElement]

inline def hideDiv(id: String): Unit =
  getDiv(id).classList.add(isHidden.value)

inline def showDiv(id: String): Unit =
  getDiv(id).classList.remove(isHidden.value)

/** Represents a DOM visibility action to be performed */
enum DomAction:
  case Hide(elementId: String)
  case Show(elementId: String)

  def execute(): Unit = this match
    case Hide(id) => hideDiv(id)
    case Show(id) => showDiv(id)

object DomAction:
  def executeAll(actions: List[DomAction]): Unit =
    actions.foreach(_.execute())

/** Pure state representing which page element is currently visible. This tracks
  * the current state without performing side effects.
  */
final case class PageVisibility(currentElementId: String):

  /** Calculate the transition to a new page. Returns None if trying to switch
    * to the initial welcome page (special case), otherwise returns the new
    * state and the DOM actions needed.
    */
  def transitionTo(
      nextElementId: String
  ): Option[(PageVisibility, List[DomAction])] =
    if nextElementId == PageMap.Pages.welcomeId then None
    else if nextElementId == currentElementId then
      // Already on this page, no transition needed
      Some((this, Nil))
    else
      Some(
        (
          PageVisibility(nextElementId),
          List(
            DomAction.Hide(currentElementId),
            DomAction.Show(nextElementId)
          )
        )
      )

object PageVisibility:
  val initial: PageVisibility = PageVisibility(PageMap.Pages.welcomeId)

/** Represents a page switch event with its associated page definition.
  * Separates pure state calculation from DOM side effects.
  */
final case class SwitchEvent(
    visibility: PageVisibility,
    currentPage: PageDef[?]
):

  /** Attempt to switch to a new page by button ID. Returns the new state and
    * DOM actions, or None if invalid.
    */
  def switchTo(buttonId: String): Option[(SwitchEvent, List[DomAction])] =
    println(PageMap)
    PageMap.findPage(buttonId).flatMap { nextPage =>
      visibility.transitionTo(nextPage.elementId).map {
        case (newVis, actions) =>
          (SwitchEvent(newVis, nextPage), actions)
      }
    }

  /** Force switch to error page */
  def switchToError: (SwitchEvent, List[DomAction]) =
    val errorPage = PageMap.errorPage
    visibility.transitionTo(errorPage.elementId) match
      case Some((newVis, actions)) =>
        (SwitchEvent(newVis, errorPage), actions)
      case None =>
        // Can't even switch to error page - stay where we are
        (this, Nil)

object SwitchEvent:
  val initial: SwitchEvent = SwitchEvent(
    PageVisibility.initial,
    PageMap.initialPage
  )

/** The application page state, combining current switch state with
  * page-specific state. This is the main state type used in the signal.
  */
final case class Page(
    switchState: SwitchEvent,
    pageState: Option[PageState]
):

  /** Handle a navigation event from a button click. Performs DOM updates as
    * side effects and returns the new Page state.
    */
  def handleNavigation(buttonId: String): Page =
    switchState.switchTo(buttonId) match
      case Some((newSwitch, actions)) =>
        DomAction.executeAll(actions)
        println(s"Switched to page: ${newSwitch.currentPage.elementId}")
        Page(
          newSwitch,
          extractPageState(newSwitch.currentPage)
        )
      case None =>
        // Invalid navigation - switch to error page
        val (errorSwitch, actions) = switchState.switchToError
        DomAction.executeAll(actions)
        println(s"Navigation failed for $buttonId, switched to error page")
        println(PageMap.findPage(buttonId))
        Page(
          errorSwitch,
          Some(PageMap.errorPage.initialState)
        )

  private def extractPageState(page: PageDef[?]): Option[PageState] =
    page.initialState match
      case PageState.NoState => None
      case s: PageState      => Some(s)

object Page:
  val initial: Page = Page(
    SwitchEvent.initial,
    None
  )

// Legacy compatibility - kept for backward compatibility during transition
@deprecated("Use Page directly", "0.1.0")
final case class LegacySwitchEvent(id: String):
  def next(nextElementId: String): Option[LegacySwitchEvent] =
    if nextElementId == PageMap.Pages.welcomeId then None
    else
      Try {
        println("Switching content from: " + id + " to: " + nextElementId)
        hideDiv(id)
        showDiv(nextElementId)
        LegacySwitchEvent(nextElementId)
      }.toOption
