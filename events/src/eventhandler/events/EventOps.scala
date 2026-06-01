package eventhandler.events

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import scala.scalajs.js.timers.{setTimeout, clearTimeout, SetTimeoutHandle}

/** Kafka-like stream composition operators for Airstream EventStreams.
  *
  * These extension methods provide familiar stream processing patterns for
  * composing, transforming, and aggregating event streams.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.events.{DomEventSource, EventOps}
  * import eventhandler.events.EventOps.given
  *
  * val clicks = DomEventSource.clicks(dom.document)
  *
  * // Accumulate to Signal (KTable-like)
  * val clickCounts = clicks.forPrefix("nav-")
  *   .fold(Map.empty[String, Int]) { (counts, click) =>
  *     val id = click.targetId.getOrElse("unknown")
  *     counts.updated(id, counts.getOrElse(id, 0) + 1)
  *   }
  *
  * // Debounce rapid events
  * val debouncedInput = inputs.debounce(300)
  *
  * // Combine streams
  * val emailChanges = changes.forId(ids.form.email).map(_.value)
  * val nameChanges = changes.forId(ids.form.name).map(_.value)
  * emailChanges.combineLatest(nameChanges).observe { case (email, name) =>
  *   updatePreview(email, name)
  * }
  * }}}
  *
  * ==Kafka Analogy==
  *
  * | Kafka Concept | EventOps Equivalent        |
  * |:--------------|:---------------------------|
  * | KTable        | fold() -> Signal[A]        |
  * | Stream join   | combineLatest(), zipWith() |
  * | Stream merge  | merge()                    |
  * | Windowing     | debounce(), throttle()     |
  */
object EventOps:

  extension [A](stream: EventStream[A])

    /** Accumulate events into a Signal (KTable-like semantics).
      *
      * Each event updates the signal with the fold function result. The signal
      * always holds the latest accumulated value.
      *
      * @param initial
      *   Initial value for the accumulator
      * @param f
      *   Fold function (accumulator, event) => newAccumulator
      * @return
      *   Signal holding the accumulated state
      */
    def fold[B](initial: B)(f: (B, A) => B)(using Owner): Signal[B] =
      stream.scanLeft(initial)(f)

    /** Merge this stream with another stream of the same or supertype.
      *
      * Events from either stream are emitted in the order they occur.
      *
      * @param other
      *   The other stream to merge with
      * @return
      *   Merged stream of events from both sources
      */
    def merge[B >: A](other: EventStream[B]): EventStream[B] =
      EventStream.merge(stream, other)

    // Note: debounce and throttle are already provided by Airstream's EventStream.
    // Use stream.debounce(delayMs) and stream.throttle(intervalMs) directly.

    /** Sample this stream whenever the sampler stream emits.
      *
      * When the sampler emits, the most recent value from this stream is
      * emitted. If this stream hasn't emitted yet, nothing happens.
      *
      * @param sampler
      *   Stream that triggers sampling
      * @return
      *   Stream of sampled values
      */
    def sampleOn[B](sampler: EventStream[B])(using Owner): EventStream[A] =
      val latest = Var(Option.empty[A])
      val _ = stream.foreach(a => latest.set(Some(a)))
      sampler.collect { case _ if latest.now().isDefined => latest.now().get }

    /** Zip with another stream, pairing events in order.
      *
      * Emits a tuple only when both streams have emitted. Events are paired in
      * the order they arrive.
      *
      * Note: Unlike combineLatest, zip requires both streams to emit before
      * producing output, and pairs are consumed once used.
      *
      * @param other
      *   The other stream to zip with
      * @return
      *   Stream of paired events
      */
    def zipWith[B](other: EventStream[B])(using
        owner: Owner
    ): EventStream[(A, B)] =
      val aQueue = scala.collection.mutable.Queue.empty[A]
      val bQueue = scala.collection.mutable.Queue.empty[B]
      val bus = new com.raquo.airstream.eventbus.EventBus[(A, B)]

      val _sub1 = stream.foreach { a =>
        if bQueue.nonEmpty then bus.writer.onNext((a, bQueue.dequeue()))
        else aQueue.enqueue(a)
      }

      val _sub2 = other.foreach { b =>
        if aQueue.nonEmpty then bus.writer.onNext((aQueue.dequeue(), b))
        else bQueue.enqueue(b)
      }

      bus.events

    /** Combine latest values from both streams.
      *
      * After both streams have emitted at least once, emits a tuple with the
      * latest value from each whenever either emits.
      *
      * @param other
      *   The other stream to combine with
      * @return
      *   Stream of combined latest values
      */
    def combineLatest[B](other: EventStream[B])(using
        Owner
    ): EventStream[(A, B)] =
      stream.combineWith(other)

    /** Take only the first N events from the stream.
      *
      * @param n
      *   Maximum number of events to emit
      * @return
      *   Stream limited to first N events
      */
    def take(n: Int)(using owner: Owner): EventStream[A] =
      var count = 0
      stream.collect {
        case a if count < n =>
          count += 1
          a
      }

    /** Drop the first N events from the stream.
      *
      * @param n
      *   Number of events to skip
      * @return
      *   Stream without the first N events
      */
    def drop(n: Int)(using Owner): EventStream[A] =
      var dropped = 0
      stream.collect {
        case a if dropped >= n => a
        case _ =>
          dropped += 1
          throw new MatchError("drop") // Skip event via collect semantics
      }

    /** Take events while the predicate holds, then stop.
      *
      * @param p
      *   Predicate to test each event
      * @return
      *   Stream that stops when predicate fails
      */
    def takeWhile(p: A => Boolean)(using Owner): EventStream[A] =
      var taking = true
      stream.collect {
        case a if taking && p(a) => a
        case a if taking && !p(a) =>
          taking = false
          throw new MatchError("takeWhile")
      }

    /** Drop events while the predicate holds, then emit all.
      *
      * @param p
      *   Predicate to test each event
      * @return
      *   Stream that starts when predicate fails
      */
    def dropWhile(p: A => Boolean)(using Owner): EventStream[A] =
      var dropping = true
      stream.collect {
        case a if !dropping => a
        case a if dropping && !p(a) =>
          dropping = false
          a
      }

    /** Emit distinct consecutive events only.
      *
      * Consecutive duplicate events are filtered out.
      *
      * @return
      *   Stream without consecutive duplicates
      */
    def distinctConsecutive(using Owner): EventStream[A] =
      var last: Option[A] = None
      stream.collect {
        case a if !last.contains(a) =>
          last = Some(a)
          a
      }

    /** Emit events paired with their predecessor.
      *
      * @return
      *   Stream of (previous, current) pairs
      */
    def withPrevious(using Owner): EventStream[(Option[A], A)] =
      var prev: Option[A] = None
      stream.map { a =>
        val result = (prev, a)
        prev = Some(a)
        result
      }
