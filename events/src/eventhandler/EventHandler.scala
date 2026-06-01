package eventhandler

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import htmlid.HtmlId
import org.scalajs.dom

// Re-export event types and sources for convenient access
export eventhandler.events.DomEvent
export eventhandler.events.DomEventSource
export eventhandler.events.EventFilter
export eventhandler.events.EventOps

// Re-export handlers
export eventhandler.handlers.ClickHandler
export eventhandler.handlers.FormHandler
export eventhandler.handlers.SelectHandler
export eventhandler.handlers.CheckboxHandler
export eventhandler.handlers.RadioGroupHandler

// Re-export router
export eventhandler.router.RouteEvent
export eventhandler.router.Router

/** EventHandler module - Scala.js only.
  *
  * Provides Kafka-like event handling capabilities for tagless HTML elements
  * using Airstream for reactive event streams and typesafe `HtmlId` references.
  *
  * ==Key Features==
  *
  *   - **Document-level event delegation**: All events captured at document
  *     root
  *   - **Type-safe ID filtering**: Filter events using `HtmlId` and `IdScope`
  *   - **Kafka-like composition**: filter, map, flatMap, fold, merge, zip
  *   - **Reactive signals**: Accumulate events into KTable-like signals
  *   - **Client-side routing**: History API integration with reactive path
  *     signals
  *
  * ==Quick Start==
  *
  * {{{
  * import eventhandler.*
  * import eventhandler.events.EventFilter.given
  * import eventhandler.events.EventOps.given
  * import htmlid.{HtmlId, IdScope}
  * import com.raquo.airstream.ownership.ManualOwner
  * import org.scalajs.dom
  *
  * object ids:
  *   object nav extends IdScope["nav"]:
  *     val home  = id["home"]
  *     val about = id["about"]
  *
  * given owner: Owner = new ManualOwner
  *
  * // All clicks at document level
  * val clicks = DomEventSource.clicks(dom.document)
  *
  * // Filter by type-safe ID
  * clicks.forId(ids.nav.home).foreach(_ => navigateHome())
  * clicks.forId(ids.nav.about).foreach(_ => navigateAbout())
  *
  * // Accumulate click counts (KTable-like)
  * val clickCounts = clicks.forPrefix("nav-")
  *   .fold(Map.empty[String, Int]) { (counts, click) =>
  *     val id = click.targetId.getOrElse("unknown")
  *     counts.updated(id, counts.getOrElse(id, 0) + 1)
  *   }
  *
  * // Router for client-side navigation
  * val router = Router.create
  * router.currentPath.observe { path =>
  *   path match
  *     case "/"       => showHome()
  *     case "/about"  => showAbout()
  *     case _         => show404()
  * }
  * }}}
  *
  * ==Kafka Analogy==
  *
  * | Kafka Concept     | EventHandler Equivalent                  |
  * |:------------------|:-----------------------------------------|
  * | Topic             | Event type (click, change, submit)       |
  * | Partition         | Element ID (via `HtmlId`)                |
  * | Consumer          | `DomEventSource` factory                 |
  * | Consumer Group    | `Owner` (subscription lifecycle)         |
  * | Message           | `DomEvent` ADT case                      |
  * | Stream Processing | Airstream operators + EventOps           |
  * | KTable            | `Signal[A]` (accumulated state via fold) |
  */
object EventHandler:

  /** Look up a DOM element by its typesafe `HtmlId`.
    *
    * This ensures that only IDs defined through the typesafe `IdScope` system
    * can be used for element lookups, preventing typo-based bugs.
    *
    * @param htmlId
    *   The type-safe ID to look up
    * @return
    *   The DOM element, or null if not found
    */
  def getElementById[S <: String](htmlId: HtmlId[S]): dom.Element =
    dom.document.getElementById(htmlId.value)

  /** Look up a DOM element by its typesafe `HtmlId`, returning an Option.
    *
    * @param htmlId
    *   The type-safe ID to look up
    * @return
    *   Some(element) if found, None otherwise
    */
  def findById[S <: String](htmlId: HtmlId[S]): Option[dom.Element] =
    Option(dom.document.getElementById(htmlId.value))

  /** Get the ID value string from an HtmlId.
    *
    * @param htmlId
    *   The type-safe ID
    * @return
    *   The ID string value
    */
  def idValue[S <: String](htmlId: HtmlId[S]): String =
    htmlId.value
