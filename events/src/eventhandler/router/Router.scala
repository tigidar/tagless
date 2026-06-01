package eventhandler.router

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.state.Var
import org.scalajs.dom
import scala.scalajs.js

/** Trait for client-side routing using the History API.
  *
  * Provides reactive signals and streams for navigation state, plus methods for
  * programmatic navigation.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.router.{Router, RouteEvent}
  * import com.raquo.airstream.ownership.ManualOwner
  *
  * given owner: Owner = new ManualOwner
  *
  * val router = Router.create
  *
  * // React to path changes
  * router.currentPath.observe { path =>
  *   path match
  *     case "/"       => showHome()
  *     case "/about"  => showAbout()
  *     case s"/users/$id" => showUser(id)
  *     case _         => show404()
  * }
  *
  * // Navigate programmatically
  * router.navigateTo("/about")
  *
  * // Replace current URL (no history entry)
  * router.replacePath("/login")
  *
  * // Handle navigation events
  * router.navigationEvents.foreach {
  *   case RouteEvent.Pop(path, _) =>
  *     // User pressed back button
  *     println(s"Went back to: $path")
  *   case _ => ()
  * }
  * }}}
  */
trait Router:

  /** Signal of the current path (without hash).
    *
    * Updates whenever navigation occurs.
    */
  def currentPath: Signal[String]

  /** Signal of the current hash (without #).
    *
    * Updates on hashchange events.
    */
  def currentHash: Signal[String]

  /** Stream of all navigation events.
    *
    * Includes pushes, pops (back/forward), replacements, and hash changes.
    */
  def navigationEvents: EventStream[RouteEvent]

  /** Navigate to a new path, adding a history entry.
    *
    * @param path
    *   The path to navigate to
    * @param state
    *   Optional state object to store in history
    */
  def navigateTo(path: String, state: Option[js.Any] = None): Unit

  /** Replace the current path without adding a history entry.
    *
    * @param path
    *   The new path
    * @param state
    *   Optional state object to store in history
    */
  def replacePath(path: String, state: Option[js.Any] = None): Unit

  /** Go back one entry in history. */
  def back(): Unit

  /** Go forward one entry in history. */
  def forward(): Unit

  /** Go to a specific point in history.
    *
    * @param delta
    *   Number of entries to go (negative = back, positive = forward)
    */
  def go(delta: Int): Unit

  /** Remove event listeners. Call this when disposing the router.
    *
    * In a typical SPA, the router lives for the entire session so this is
    * usually not needed. But if you need to clean up, call this method.
    */
  def removeListeners(): Unit

object Router:

  /** Create a Router instance.
    *
    * The router immediately starts listening for popstate and hashchange
    * events.
    *
    * @param owner
    *   Owner for managing event subscriptions
    * @return
    *   A new Router instance
    */
  def create(using owner: Owner): Router =
    new RouterImpl()

  private class RouterImpl(using owner: Owner) extends Router:

    private val eventBus = new EventBus[RouteEvent]
    private val pathVar = Var(currentPathFromWindow)
    private val hashVar = Var(currentHashFromWindow)

    // Listen for popstate (back/forward buttons)
    private val popstateListener: js.Function1[dom.PopStateEvent, Unit] = {
      event =>
        val path = currentPathFromWindow
        val state = Option(event.state)
        pathVar.set(path)
        eventBus.writer.onNext(RouteEvent.Pop(path, state))
    }
    dom.window.addEventListener("popstate", popstateListener)

    // Listen for hashchange
    private val hashchangeListener: js.Function1[dom.HashChangeEvent, Unit] = {
      event =>
        val oldHash = extractHash(event.oldURL)
        val newHash = extractHash(event.newURL)
        hashVar.set(newHash)
        eventBus.writer.onNext(RouteEvent.HashChange(oldHash, newHash))
    }
    dom.window.addEventListener("hashchange", hashchangeListener)

    // Note: Event listeners remain attached for the lifetime of the page.
    // In a typical SPA, the router lives for the entire session.
    // For cleanup, users can call removeListeners() explicitly.

    /** Remove event listeners manually. Call this when disposing the router. */
    def removeListeners(): Unit =
      dom.window.removeEventListener("popstate", popstateListener)
      dom.window.removeEventListener("hashchange", hashchangeListener)

    def currentPath: Signal[String] = pathVar.signal

    def currentHash: Signal[String] = hashVar.signal

    def navigationEvents: EventStream[RouteEvent] = eventBus.events

    def navigateTo(path: String, state: Option[js.Any] = None): Unit =
      val stateValue = state.getOrElse(null)
      dom.window.history.pushState(stateValue, "", path)
      pathVar.set(path)
      eventBus.writer.onNext(RouteEvent.Push(path, state))

    def replacePath(path: String, state: Option[js.Any] = None): Unit =
      val stateValue = state.getOrElse(null)
      dom.window.history.replaceState(stateValue, "", path)
      pathVar.set(path)
      eventBus.writer.onNext(RouteEvent.Replace(path, state))

    def back(): Unit =
      dom.window.history.back()

    def forward(): Unit =
      dom.window.history.forward()

    def go(delta: Int): Unit =
      dom.window.history.go(delta)

    private def currentPathFromWindow: String =
      dom.window.location.pathname

    private def currentHashFromWindow: String =
      val hash = dom.window.location.hash
      if hash.startsWith("#") then hash.drop(1) else hash

    private def extractHash(url: String): String =
      val hashIndex = url.indexOf('#')
      if hashIndex >= 0 then url.drop(hashIndex + 1) else ""
