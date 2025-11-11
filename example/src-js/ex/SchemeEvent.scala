package ex

import org.scalajs.dom

object SchemeEvent:

  def setScheme(value: String) =
    println("############### something happend ###############")
    println(s"value: ${value}")
    if value == "" || value == null then
      dom.document.documentElement.removeAttribute("data-theme")
    else dom.document.documentElement.setAttribute("data-theme", value)
