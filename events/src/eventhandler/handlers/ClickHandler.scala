package eventhandler.handlers

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.ownership.Owner
import htmlid.{HtmlId, IdScope}
import eventhandler.events.{DomEvent, DomEventSource, EventFilter}
import org.scalajs.dom

/** Scoped click handler for managing button clicks within an ID scope.
  *
  * Provides a convenient way to handle clicks on elements that share a common
  * ID prefix (scope). All clicks are captured at the document level and
  * filtered by the scope prefix.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.handlers.ClickHandler
  * import htmlid.IdScope
  *
  * object ids:
  *   object nav extends IdScope["nav"]:
  *     val home  = id["home"]   // HtmlId["nav-home"]
  *     val about = id["about"]  // HtmlId["nav-about"]
  *     val contact = id["contact"]
  *
  * given owner: Owner = new ManualOwner
  *
  * // Create handler for all nav- prefixed clicks
  * val navHandler = ClickHandler.forPrefix("nav-")
  *
  * // Register handlers for specific IDs
  * navHandler.on(ids.nav.home) { click =>
  *   click.preventDefault()
  *   navigateHome()
  * }
  *
  * navHandler.on(ids.nav.about) { click =>
  *   navigateAbout()
  * }
  *
  * // Get stream for custom processing
  * navHandler.streamFor(ids.nav.contact).foreach { click =>
  *   showContactModal()
  * }
  * }}}
  */
class ClickHandler private (
    prefix: String,
    target: dom.EventTarget,
    useCapture: Boolean
)(using owner: Owner):

  import EventFilter.*

  /** The underlying stream of all clicks within this scope. */
  val allClicks: EventStream[DomEvent.Click] =
    DomEventSource.clicks(target, useCapture).forPrefix(prefix)

  /** Register a handler for clicks on a specific element ID.
    *
    * The handler is called for each click event on the element.
    *
    * @param htmlId
    *   The type-safe HtmlId to handle
    * @param handler
    *   Callback function receiving the click event
    */
  def on[S <: String](htmlId: HtmlId[S])(
      handler: DomEvent.Click => Unit
  ): Unit =
    val _ = allClicks.forId(htmlId).foreach(handler)

  /** Get an event stream for clicks on a specific element ID.
    *
    * Use this for more complex stream processing.
    *
    * @param htmlId
    *   The type-safe HtmlId to filter for
    * @return
    *   Stream of clicks only from the specified element
    */
  def streamFor[S <: String](htmlId: HtmlId[S]): EventStream[DomEvent.Click] =
    allClicks.forId(htmlId)

  /** Handle all clicks within the scope.
    *
    * @param handler
    *   Callback function receiving the click event
    */
  def onAny(handler: DomEvent.Click => Unit): Unit =
    val _ = allClicks.foreach(handler)

object ClickHandler:

  /** Create a ClickHandler for clicks filtered by prefix string.
    *
    * @param prefix
    *   The ID prefix to filter (e.g., "nav-")
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def forPrefix(
      prefix: String,
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using Owner): ClickHandler =
    new ClickHandler(prefix, target, useCapture)

  /** Create a stream of all clicks on the document.
    *
    * @param target
    *   DOM element to listen on (default: document)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def all(
      target: dom.EventTarget = dom.document,
      useCapture: Boolean = true
  )(using Owner): EventStream[DomEvent.Click] =
    DomEventSource.clicks(target, useCapture)
