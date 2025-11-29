package ex
//import com.raquo.airstream.eventbus.EventBusStream
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.{Owner, ManualOwner}
import org.scalajs.dom
import org.scalajs.dom.{HTMLDivElement, HTMLAnchorElement}
import org.scalajs.dom.html
import org.scalajs.dom.document
import org.scalajs.dom.Element
import PageMap.Pages.pageIds

object MouseEvents:
  val onClick = "onclick"
  val onMouseOver = "onmouseover"
  val onMouseOut = "onmouseout"
  val onFocus = "onfocus"
  val onBlur = "onblur"

object AnchorEvents:

  val pageEventBus = new EventBus[PageEvent]
  val pageEvent = PageEvent.initialPage

  def submit(a: html.Anchor)(using o: Owner): Unit =
    println(s"Anchor clicked: ${a.id}, href: ${a.href}")
    EventBus.emit(
      pageEventBus -> pageEvent.switchTo(a.id)
    )
