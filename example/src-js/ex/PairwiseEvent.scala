package ex

import org.scalajs.dom.{HTMLElement, HTMLDivElement}
import org.scalajs.dom
import scala.util.Try
import org.scalajs.dom.document
import com.raquo.airstream.core.{Observer, EventStream, Signal}
import com.raquo.airstream.ownership.{Owner, ManualOwner}

/** A generic state transition tracker that distinguishes between:
  *   - Initial state (never seen any input)
  *   - Subsequent states (have processed at least one input)
  *
  * This is the "Mealy machine" or "pairwise" pattern from FP. It allows
  * detecting first-time transitions vs repeated states.
  *
  * @tparam S
  *   The state type
  * @tparam A
  *   The side-effect/action type
  */
enum StateTransition[+S, +A]:
  /** Initial state before any events have been processed */
  case Initial(state: S)

  /** State after processing at least one event, with optional actions */
  case Transitioned(previous: S, current: S, actions: List[A])

  def currentState: S = this match
    case Initial(s)               => s
    case Transitioned(_, curr, _) => curr

  def isInitial: Boolean = this match
    case Initial(_)            => true
    case Transitioned(_, _, _) => false

  def hasChanged(using CanEqual[S, S]): Boolean = this match
    case Initial(_)                  => false
    case Transitioned(prev, curr, _) => prev != curr

object StateTransition:
  /** Create a transition function that tracks state changes. On first call,
    * returns Transitioned with the initial state as "previous". This ensures
    * the first event always triggers actions.
    *
    * @param initialState
    *   The starting state
    * @param transition
    *   Function that computes (newState, actions) from current state and input
    */
  def folder[S, I, A](
      initialState: S,
      transition: (S, I) => (S, List[A])
  ): (StateTransition[S, A], I) => StateTransition[S, A] =
    (current, input) =>
      val currentState = current.currentState
      val (newState, actions) = transition(currentState, input)
      StateTransition.Transitioned(currentState, newState, actions)

  /** Alternative: only emit actions when state actually changes */
  def distinctFolder[S, I, A](
      initialState: S,
      transition: (S, I) => (S, List[A])
  )(using CanEqual[S, S]): (StateTransition[S, A], I) => StateTransition[S, A] =
    (current, input) =>
      val currentState = current.currentState
      val (newState, actions) = transition(currentState, input)
      // Only include actions if state changed or this is the first transition
      val effectiveActions =
        if current.isInitial || currentState != newState then actions
        else Nil
      StateTransition.Transitioned(currentState, newState, effectiveActions)

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

/** Pure state representing which page element is currently visible. Uses Option
  * to distinguish "no page shown yet" from "showing a page". This allows proper
  * handling of the initial page display.
  */
final case class PageVisibility(currentElementId: Option[String]):

  /** Calculate the transition to a new page. Returns None if trying to switch
    * to the initial welcome page (special case), otherwise returns the new
    * state and the DOM actions needed.
    */
  def transitionTo(nextElementId: String): (PageVisibility, List[DomAction]) =
    currentElementId match
      case None =>
        // First page display - just show, no hide needed
        (
          PageVisibility(Some(nextElementId)),
          List(DomAction.Show(nextElementId))
        )
      case Some(current) if current == nextElementId =>
        // Already on this page, no transition needed
        (this, Nil)
      case Some(current) =>
        // Normal transition - hide current, show next
        (
          PageVisibility(Some(nextElementId)),
          List(DomAction.Hide(current), DomAction.Show(nextElementId))
        )

object PageVisibility:
  /** Initial state: no page is currently visible */
  val initial: PageVisibility = PageVisibility(None)

/** Represents a page switch event with its associated page definition.
  * Separates pure state calculation from DOM side effects.
  */
final case class SwitchEvent(
    visibility: PageVisibility,
    currentPage: PageDef[?]
):

  /** Attempt to switch to a new page by button ID. Returns the new state and
    * DOM actions, or None if the page wasn't found.
    */
  def switchTo(buttonId: String): Option[(SwitchEvent, List[DomAction])] =
    PageMap.findPage(buttonId).map { nextPage =>
      val (newVis, actions) = visibility.transitionTo(nextPage.elementId)
      (SwitchEvent(newVis, nextPage), actions)
    }

  /** Force switch to error page */
  def switchToError: (SwitchEvent, List[DomAction]) =
    val errorPage = PageMap.errorPage
    val (newVis, actions) = visibility.transitionTo(errorPage.elementId)
    (SwitchEvent(newVis, errorPage), actions)

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
