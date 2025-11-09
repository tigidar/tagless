package ex

import org.scalajs.dom

object SchemeEvent:

  def setScheme(value: String) =
    println("running")
    dom.document.documentElement.setAttribute("data-theme", value)
