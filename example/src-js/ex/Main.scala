package ex

import org.scalajs.dom
import org.scalajs.dom.{HTMLAnchorElement, html}
import com.raquo.airstream.eventbus.EventBus

@main def main(): Unit = {
  Events.init()
  val home = ex.get[HTMLAnchorElement]("home-button")
  val event =
    Events.anchorEvents.pageEventBus -> home

  EventBus.emit(
    event
  )
}
