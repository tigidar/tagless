package ex

import org.scalajs.dom

@main def main(): Unit = {
  println("Hello, Scala 3!, it works ... also with hot reload!")
  println("###############################")
  println("Starting application...")
  Events.init()
  AnchorEvents.toggleAnchorEvent("home-button")
  /*
  val click = dom.document
    .getElementById("home-button")
    .dispatchEvent(
      new dom.MouseEvent("click", null)
    )
   */
}
