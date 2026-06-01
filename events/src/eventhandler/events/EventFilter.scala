package eventhandler.events

import com.raquo.airstream.core.EventStream
import htmlid.HtmlId

/** Extension methods for filtering event streams by element ID.
  *
  * These operators enable Kafka-like partitioning of event streams based on the
  * type-safe `HtmlId` system. Events can be filtered by exact ID match, ID
  * prefix (for scoped hierarchies), or custom predicates.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.events.{DomEventSource, EventFilter}
  * import eventhandler.events.EventFilter.given
  * import htmlid.{HtmlId, IdScope}
  *
  * object ids:
  *   object nav extends IdScope["nav"]:
  *     val home  = id["home"]   // HtmlId["nav-home"]
  *     val about = id["about"]  // HtmlId["nav-about"]
  *
  * val clicks = DomEventSource.clicks(dom.document)
  *
  * // Filter by exact ID (type-safe)
  * clicks.forId(ids.nav.home).foreach(_ => navigateHome())
  *
  * // Filter by prefix (for all nav clicks)
  * clicks.forPrefix("nav-").foreach(click => handleNav(click.targetId))
  *
  * // Custom predicate
  * clicks.filterById(_.startsWith("btn-")).foreach(handleButton)
  *
  * // Pair with typed ID for pattern matching
  * clicks.withId(ids.nav.home).foreach { case (id, click) =>
  *   println(s"Clicked: ${id.value}")
  * }
  * }}}
  *
  * ==Kafka Analogy==
  *
  * | Kafka Concept | EventFilter Equivalent   |
  * |:--------------|:-------------------------|
  * | Partition     | Element ID (via HtmlId)  |
  * | Consumer      | forId / forPrefix filter |
  * | Key Filter    | filterById predicate     |
  */
object EventFilter:

  /** Extension methods for filtering DomEvent streams by ID. */
  extension [E <: DomEvent](stream: EventStream[E])

    /** Filter events to only those from an element with the specified exact ID.
      *
      * This is the type-safe way to filter events, using the `HtmlId` system to
      * ensure only valid IDs defined in your scope hierarchy are used.
      *
      * @param htmlId
      *   The type-safe HtmlId to match against
      * @return
      *   Stream of events only from elements with the matching ID
      */
    def forId[S <: String](htmlId: HtmlId[S]): EventStream[E] =
      stream.filter(_.targetId.contains(htmlId.value))

    /** Filter events to only those from elements whose ID starts with the
      * prefix.
      *
      * Useful for handling all events from a scoped hierarchy, e.g., all
      * navigation clicks with IDs like "nav-home", "nav-about", etc.
      *
      * @param prefix
      *   The ID prefix to match against
      * @return
      *   Stream of events from elements whose ID starts with the prefix
      */
    def forPrefix(prefix: String): EventStream[E] =
      stream.filter(_.targetId.exists(_.startsWith(prefix)))

    /** Filter events using a custom predicate on the element ID.
      *
      * The predicate receives the ID string. Events from elements without IDs
      * are excluded.
      *
      * @param pred
      *   Predicate function applied to the ID string
      * @return
      *   Stream of events matching the predicate
      */
    def filterById(pred: String => Boolean): EventStream[E] =
      stream.filter(_.targetId.exists(pred))

    /** Pair each event with the typed HtmlId if it matches.
      *
      * Only events with matching IDs pass through. The result is a tuple of the
      * typed HtmlId and the event, enabling type-safe handling.
      *
      * @param htmlId
      *   The type-safe HtmlId to match against
      * @return
      *   Stream of (HtmlId, Event) tuples for matching events
      */
    def withId[S <: String](htmlId: HtmlId[S]): EventStream[(HtmlId[S], E)] =
      stream.collect {
        case e if e.targetId.contains(htmlId.value) => (htmlId, e)
      }

    /** Filter events excluding those with the specified ID.
      *
      * @param htmlId
      *   The type-safe HtmlId to exclude
      * @return
      *   Stream of events not from the specified ID
      */
    def notId[S <: String](htmlId: HtmlId[S]): EventStream[E] =
      stream.filter(!_.targetId.contains(htmlId.value))

    /** Filter events to only those that have an ID attribute.
      *
      * @return
      *   Stream of events only from elements with IDs
      */
    def withAnyId: EventStream[E] =
      stream.filter(_.targetId.isDefined)

    /** Filter events to only those without an ID attribute.
      *
      * @return
      *   Stream of events only from elements without IDs
      */
    def withoutId: EventStream[E] =
      stream.filter(_.targetId.isEmpty)
