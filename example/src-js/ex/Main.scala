package ex

import org.scalajs.dom
import com.raquo.airstream.eventbus.EventBus

@main def main(): Unit = {
  println("Hello, Scala 3!, it works ... also with hot reload!")
  println("###############################")
  println("Starting application...")
  Events.init()
  // TODO: this is not ideal, since it will flicker the page on load, maybe just fade in fade out?
  EventBus.emit(
    AnchorEvents.pageEventBus -> AnchorEvents.pageEvent.switchTo("home-button")
  )
}
