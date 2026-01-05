package ex
//import com.raquo.airstream.eventbus.EventBusStream
import com.raquo.airstream.core.{EventStream, Signal, Observer}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.{Owner, ManualOwner}
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLAnchorElement}
import org.scalajs.dom.html
import org.scalajs.dom.document
import org.scalajs.dom.Element
import com.raquo.airstream.state.Var

object MouseEvents:
  val onClick = "onclick"
  val onMouseOver = "onmouseover"
  val onMouseOut = "onmouseout"
  val onFocus = "onfocus"
  val onBlur = "onblur"

final case class AnchorEvents()(using o: Owner):

  /* Var alternative, it implies mutable state which can be okay in some cases
  val pageEvent = Var(PageEvent.initialPage)
  val pageObserver: Observer[String] =
    Observer[String] { anchorId =>
      pageEvent.update { current =>
        current.switchTo(anchorId)
      }
    }

  val obs = pageEventBus.events.addObserver(pageObserver)
   */

  val pageEventBus = new EventBus[html.Anchor]

  val pageStateSignal: Signal[Page] =
    pageEventBus.events
      .scanLeft(Page.initialPage) { (current, anchor) =>
        println("AnchorEvents.pageStateSignal.scanLeft:")
        current.switchTo(anchor.id)
      }

  /* We need at least one observer to actually activate the signal
  so it can be actually triggered. In our case, it is useful because
  we can do a good work on browser history etc.
   */
  val obs = pageStateSignal.addObserver(
    Observer[Page](pe =>
      println(s"Current page switched to: ${pe.pageSwitch.id}")
    )
  )

  def submit(a: html.Anchor): Unit =
    println(s"changed url to : ${a}")
    EventBus.emit(
      pageEventBus -> a
    )
