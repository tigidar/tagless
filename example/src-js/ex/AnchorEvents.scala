package ex
//import com.raquo.airstream.eventbus.EventBusStream
import com.raquo.airstream.core.EventStream
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.document
import org.scalajs.dom.Element
import PageMap.Pages.pageIds

object MouseEvents:
  val onClick = "onclick"
  val onMouseOver = "onmouseover"
  val onMouseOut = "onmouseout"
  val onFocus = "onfocus"
  val onBlur = "onblur"

//val anchorIds: EventStream[String] =
val anchorIds =
  Events.clickEvents
    .map(_.asInstanceOf[dom.MouseEvent])
    .map { e =>
      val el = e.target.asInstanceOf[dom.Element]
      Option(el.closest("a[id]")).collect { case a: dom.HTMLAnchorElement =>
        (e, a)
      }
    }
    .collect { case Some((e, a)) =>
      val isNewTab =
        a.target == "_blank" || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button == 1
      if (!isNewTab) {
        e.preventDefault()
        e.stopPropagation()
        a.id
      } else null
    }
    .filter(_ != null) // keep only handled anchors
    .filter(_.nonEmpty)
    .collect { s =>
      AnchorEvents.router(s)
    } // map anchor-id -> content-id
    .distinct

inline def getDiv(id: String): HTMLDivElement =
  dom.document.getElementById(id).asInstanceOf[HTMLDivElement]

inline def hideDiv(id: String): Unit =
  getDiv(id).classList.add("is-hidden")

inline def showDiv(id: String): Unit =
  getDiv(id).classList.remove("is-hidden")

object AnchorEvents:

  val router = Map(
    "home-button" -> "welcome-content",
    "about-button" -> "about-content"
  )

  def togglePageEvent(anchorId: String) =
    pageIds.foreach { id =>
      if id == router(anchorId) then showDiv(id)
      else hideDiv(id)
    }

  def toggleAnchorEvent(anchorId: String) =
    if router.keySet.contains(anchorId) then togglePageEvent(anchorId)
    else println(s"Anchor event: Unregistered anchor id: ${anchorId}")
