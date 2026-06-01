package eventhandler.events

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Observer
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.eventbus.EventBus
import org.scalajs.dom

/** Factory for creating Airstream event streams from document-level DOM events.
  *
  * This is the primary entry point for the Kafka-like event processing pattern.
  * All events are captured at the document level using event
  * capturing/bubbling, enabling centralized event handling without attaching
  * handlers to individual elements.
  *
  * ==Key Design Principles==
  *
  *   - **Document-level delegation**: Events captured at document root, not on
  *     individual elements
  *   - **Capture phase by default**: `useCapture = true` ensures events are
  *     caught before they can be stopped
  *   - **Type-safe extraction**: Raw DOM events are wrapped in the `DomEvent`
  *     ADT with extracted metadata
  *   - **Reactive streams**: Returns Airstream `EventStream` for composition
  *     with filter, map, merge, etc.
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.events.{DomEventSource, DomEvent}
  * import com.raquo.airstream.ownership.ManualOwner
  *
  * given owner: Owner = new ManualOwner
  *
  * // All clicks on the document
  * val clicks = DomEventSource.clicks(dom.document)
  *
  * // Filter by element ID
  * clicks.filter(_.targetId.contains("submit-btn")).foreach { click =>
  *   click.preventDefault()
  *   handleSubmit()
  * }
  *
  * // Form submissions
  * val submits = DomEventSource.submits(dom.document)
  * submits.foreach { submit =>
  *   submit.preventDefault()
  *   processForm(submit.target)
  * }
  * }}}
  *
  * ==Kafka Analogy==
  *
  * | Kafka Concept  | DomEventSource Equivalent              |
  * |:---------------|:---------------------------------------|
  * | Topic          | Event type (clicks, changes, submits)  |
  * | Consumer       | DomEventSource factory methods         |
  * | Consumer Group | Owner (manages subscription lifecycle) |
  * | Message        | DomEvent ADT case                      |
  */
object DomEventSource:

  /** Create a stream of click events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def clicks(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Click] =
    fromEventType[dom.MouseEvent](target, "click", useCapture).map { event =>
      // Get the actual clicked element
      val rawTarget = event.target match
        case el: dom.Element => el
        case _               => null

      // Find the nearest ancestor (or self) with an ID for event delegation
      val closestWithId =
        Option(rawTarget).flatMap(el => Option(el.closest("[id]")))
      val id =
        closestWithId.map(_.asInstanceOf[dom.Element].id).filter(_.nonEmpty)

      DomEvent.Click(event, id, rawTarget)
    }

  /** Create a stream of change events from the target.
    *
    * This handles generic change events. For checkboxes and selects, use
    * `checkboxChanges` or `selectChanges` for specialized handling.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def changes(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Change] =
    fromEventType[dom.Event](target, "change", useCapture).map { event =>
      val targetEl = event.target.asInstanceOf[dom.Element]
      val id = Option(targetEl.id).filter(_.nonEmpty)
      val value = targetEl match
        case input: dom.html.Input       => input.value
        case textarea: dom.html.TextArea => textarea.value
        case select: dom.html.Select     => select.value
        case _                           => ""
      DomEvent.Change(event, id, targetEl, value)
    }

  /** Create a stream of checkbox change events from the target.
    *
    * Filters for checkbox inputs and provides the checked state.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def checkboxChanges(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.CheckboxChange] =
    fromEventType[dom.Event](target, "change", useCapture).collect {
      case event
          if event.target.isInstanceOf[dom.html.Input] &&
            event.target.asInstanceOf[dom.html.Input].`type` == "checkbox" =>
        val input = event.target.asInstanceOf[dom.html.Input]
        val id = Option(input.id).filter(_.nonEmpty)
        DomEvent.CheckboxChange(event, id, input.checked)
    }

  /** Create a stream of select change events from the target.
    *
    * Filters for select elements and provides selected value and index.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def selectChanges(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.SelectChange] =
    fromEventType[dom.Event](target, "change", useCapture).collect {
      case event if event.target.isInstanceOf[dom.html.Select] =>
        val select = event.target.asInstanceOf[dom.html.Select]
        val id = Option(select.id).filter(_.nonEmpty)
        DomEvent.SelectChange(event, id, select.value, select.selectedIndex)
    }

  /** Create a stream of form submit events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def submits(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Submit] =
    fromEventType[dom.Event](target, "submit", useCapture).collect {
      case event if event.target.isInstanceOf[dom.html.Form] =>
        val form = event.target.asInstanceOf[dom.html.Form]
        val id = Option(form.id).filter(_.nonEmpty)
        DomEvent.Submit(event, id, form)
    }

  /** Create a stream of input events from the target.
    *
    * Input events fire on every keystroke, unlike change events which fire on
    * blur.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def inputs(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Input] =
    fromEventType[dom.Event](target, "input", useCapture).map { event =>
      val targetEl = event.target.asInstanceOf[dom.Element]
      val id = Option(targetEl.id).filter(_.nonEmpty)
      val value = targetEl match
        case input: dom.html.Input       => input.value
        case textarea: dom.html.TextArea => textarea.value
        case _                           => ""
      DomEvent.Input(event, id, value)
    }

  /** Create a stream of keydown events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def keydowns(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.KeyDown] =
    fromEventType[dom.KeyboardEvent](target, "keydown", useCapture).map {
      event =>
        val targetEl = event.target.asInstanceOf[dom.Element]
        val id = Option(targetEl.id).filter(_.nonEmpty)
        DomEvent.KeyDown(event, id, event.key, event.code)
    }

  /** Create a stream of keyup events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true)
    */
  def keyups(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.KeyUp] =
    fromEventType[dom.KeyboardEvent](target, "keyup", useCapture).map { event =>
      val targetEl = event.target.asInstanceOf[dom.Element]
      val id = Option(targetEl.id).filter(_.nonEmpty)
      DomEvent.KeyUp(event, id, event.key, event.code)
    }

  /** Create a stream of focus events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true for focus, which doesn't
    *   bubble)
    */
  def focuses(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Focus] =
    fromEventType[dom.FocusEvent](target, "focus", useCapture).map { event =>
      val targetEl = event.target.asInstanceOf[dom.Element]
      val id = Option(targetEl.id).filter(_.nonEmpty)
      DomEvent.Focus(event, id)
    }

  /** Create a stream of blur events from the target.
    *
    * @param target
    *   DOM element to listen on (typically `dom.document`)
    * @param useCapture
    *   Whether to use capture phase (default: true for blur, which doesn't
    *   bubble)
    */
  def blurs(target: dom.EventTarget, useCapture: Boolean = true)(using
      Owner
  ): EventStream[DomEvent.Blur] =
    fromEventType[dom.FocusEvent](target, "blur", useCapture).map { event =>
      val targetEl = event.target.asInstanceOf[dom.Element]
      val id = Option(targetEl.id).filter(_.nonEmpty)
      DomEvent.Blur(event, id)
    }

  /** Create a raw event stream for any event type.
    *
    * This is the low-level factory used by all specialized methods. Use this
    * for custom event types not covered by the typed methods.
    *
    * @param target
    *   DOM element to listen on
    * @param eventType
    *   Event type string (e.g., "click", "change", "custom-event")
    * @param useCapture
    *   Whether to use capture phase (default: true)
    * @tparam E
    *   The specific DOM event type
    */
  def fromEventType[E <: dom.Event](
      target: dom.EventTarget,
      eventType: String,
      useCapture: Boolean = true
  )(using owner: Owner): EventStream[E] =
    val bus = new EventBus[E]

    val listener: scalajs.js.Function1[E, Unit] = (event: E) =>
      bus.writer.onNext(event)

    target.addEventListener(eventType, listener, useCapture)

    // Note: Cleanup happens when the EventBus is garbage collected
    // or when the subscription from foreach/observe is killed.
    // For manual cleanup, users should track and remove listeners themselves.

    bus.events
