package ex

import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.core.Observer
import com.raquo.airstream.ownership.{Owner, ManualOwner}
import org.scalajs.dom
import org.scalajs.dom.html.Body
import scala.scalajs.js.JSON
import com.raquo.airstream.web.DomEventStream
import org.scalajs.dom.html

object Events:

  val valuesEventBus = new EventBus[Int]
  val labelsEventBus = new EventBus[String]
  given owner: Owner = ManualOwner.apply()

  inline def get(id: String) =
    dom.document.getElementById(id)

  val pageChangeEvents =
    DomEventStream(dom.document, "change", useCapture = true)

  val pageSubmitEvents =
    DomEventStream(dom.document, "submit", useCapture = true)

  val pageClickEvents =
    DomEventStream(dom.document, "click", useCapture = true)

  def init() =

    // valuesEventBus.emit(e.clientX)
    // labelsEventBus.emit(s"X: ${e.clientX}, Y: ${e.clientY}")
    val obs1 = Observer[Int](newValue => println(newValue))
    val obs2 = Observer[String](newValue => println(newValue))

    pageChangeEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case b: Body =>
            println(s"Body event: ${b.id}, ${b.className}, ${b.innerHTML}")
          case sel: html.Select =>
            SchemeEvent.setScheme(sel.value)
            println(
              s"Select event: ${sel.id}, ${sel.className}, ${sel.value}, ${sel.selectedIndex}"
            )

          case e =>
            println(s"Unregistered event: ${e}")
      }
    )

    pageSubmitEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case f: html.Form =>
            println(s"Form event: ${f.id}, ${f.className}, ${f.action}")
            e.preventDefault()
          case b: html.Button =>
            println(s"Button event: ${b.id}, ${b.className}, ${b.value}")
            e.preventDefault()
          case _ =>
            println(s"Other event: ${e.target}")
            e.preventDefault()
      }
    )

    pageClickEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case b: html.Button =>
            println(s"Button event: ${b.id}, ${b.className}, ${b.value}")
            e.preventDefault()
          case a: html.Anchor =>
            println(s"Anchor event: ${a.id}, ${a.className}, ${a.href}")
            e.preventDefault()
          case _ =>
            println(s"Other event: ${e.target}")
          // e.preventDefault()
      }
    )

    dom.document.onsubmit = (e: dom.Event) =>
      e.target match
        case f: html.Form =>
          println(s"Form event2: ${f.id}, ${f.className}, ${f.action}")
          e.preventDefault()
        case b: html.Button =>
          println(s"Button event2: ${b.id}, ${b.className}, ${b.value}")
          e.preventDefault()
        case b: html.Anchor =>
          println(s"Button event2: ${b.id}, ${b.className}")
          e.preventDefault()
        case _ =>
          println(s"Other event2: ${e.target}")
          e.preventDefault()

    val s1 = valuesEventBus.events.addObserver(obs1)
    val s2 = labelsEventBus.events.addObserver(obs2)

    EventBus.emit(
      valuesEventBus -> 100,
      labelsEventBus -> "users"
    )

    // s1.kill()
