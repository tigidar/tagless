package ex

import org.scalajs.dom
import org.scalajs.dom.{HTMLAnchorElement, html}
import org.scalajs.dom.window
import com.raquo.airstream.eventbus.EventBus

@main def main(): Unit = {
  Events.init()

  // Handle initial URL or default to home
  val initialPath = window.location.pathname
  val buttonId = UrlRoutes.buttonIdForPath(initialPath).getOrElse(PageMap.NavButtons.homeId)

  Option(dom.document.getElementById(buttonId)) match
    case Some(anchor: HTMLAnchorElement) =>
      println(s"Initial navigation to: $buttonId (path: $initialPath)")
      EventBus.emit(Events.anchorEvents.pageEventBus -> anchor)
    case _ =>
      // Fallback to home button
      val home = ex.get[HTMLAnchorElement](PageMap.NavButtons.homeId)
      EventBus.emit(Events.anchorEvents.pageEventBus -> home)
}
