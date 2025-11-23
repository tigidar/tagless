package ex

import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.core.{Observer, EventStream}
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

  val changeEvents =
    DomEventStream(dom.document, "change", useCapture = true)

  val submitEvents =
    DomEventStream(dom.document, "submit", useCapture = true)

  val clickEvents =
    DomEventStream(dom.document, "click", useCapture = true)

  // Map global clicks to *content ids* we own, prevent navigation
  val contentIds: EventStream[String] =
    clickEvents
      .map(_.asInstanceOf[dom.MouseEvent])
      .map { e =>
        val el = e.target.asInstanceOf[dom.Element]
        Option(el.closest("a[id]")).collect { case a: dom.HTMLAnchorElement =>
          (e, a)
        }
      }
      .collect { case Some((e, a)) =>
        val newTab =
          a.target == "_blank" || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button == 1
        if (!newTab) { e.preventDefault(); e.stopPropagation(); a.id }
        else null
      }
      .filter(_ != null)
      .collect(AnchorEvents.router) // anchor-id -> content-id
      .distinct

  // TODO: investigate if this actually works as intended!!!
  def pairwise[A](s: EventStream[A]): EventStream[(Option[A], A)] =
    s.scanLeft[(Option[A], Option[A])]((None, None)) { case ((_, last), cur) =>
      (last, Some(cur))
    }.flatMapSwitch {
      case (Some(prev), Some(cur)) => EventStream.fromValue((Some(prev), cur))
      case (None, Some(cur))       => EventStream.fromValue((None, cur))
      case _                       => EventStream.empty
    }

  val toggleStream: EventStream[Unit] =
    pairwise(contentIds).map { case (prevOpt, cur) =>
      prevOpt.foreach(hideDiv) // deactivate previous (if any)
      showDiv(cur) // activate current
    }

// owner.killSubscriptions() when tearing down
  def init() =

    val s = toggleStream.addObserver(Observer(_ => ()))(using owner)

    // valuesEventBus.emit(e.clientX)
    // labelsEventBus.emit(s"X: ${e.clientX}, Y: ${e.clientY}")
    val obs1 = Observer[Int](newValue => println(newValue))
    val obs2 = Observer[String](newValue => println(newValue))

    val subscription = changeEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case sel: html.Select =>
            println(s"Select value:  ${sel.value}")
            SchemeEvent.setScheme(sel.value)
            println(
              s"Select event: ${sel.id}, ${sel.className}, ${sel.value}, ${sel.selectedIndex}"
            )
          case b: Body =>
            println(s"Body event: ${b.id}, ${b.className}, ${b.innerHTML}")

          case e =>
            println(s"Unregistered event: ${e}")
      }
    )

    val changeSubsription = submitEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case f: html.Form =>
            println(s"Form event: ${f.id}, ${f.className}, ${f.action}")
            e.preventDefault()
          case b: html.Button =>
            println(s"Button event: ${b.id}, ${b.className}, ${b.value}")
            e.preventDefault()
          case _ =>
            println(s"Other Submit event: ${e.target}")
            e.preventDefault()
      }
    )

    val clickSubscription = clickEvents.addObserver(
      Observer[dom.Event] { e =>
        e.target match
          case b: html.Button =>
            println(s"Button event: ${b.id}, ${b.className}, ${b.value}")
            e.preventDefault()
          case a: html.Anchor =>
            println(s"Anchor event: ${a.id}, ${a.className}, ${a.href}")
            println("step 2")
            AnchorEvents.toggleAnchorEvent(a.id)
            e.preventDefault()
            e.stopPropagation()
          case _ =>
            println(s"Other Click event: ${e.target}")
          // e.preventDefault()
      }
    )

    val submitSubscription = dom.document.onsubmit = (e: dom.Event) =>
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
          println(s"Other  Submit event2: ${e.target}")
          e.preventDefault()

    val s1 = valuesEventBus.events.addObserver(obs1)
    val s2 = labelsEventBus.events.addObserver(obs2)

    EventBus.emit(
      valuesEventBus -> 100,
      labelsEventBus -> "users"
    )

    // s1.kill()
