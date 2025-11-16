package ex

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

def getDiv(id: String): HTMLDivElement =
  dom.document.getElementById(id).asInstanceOf[HTMLDivElement]

object AnchorEvents:

  val router = Map(
    "home-button" -> "welcome-content",
    "about-button" -> "about-content"
  )

  def togglePageEvent(anchorId: String) =
    pageIds.foreach { id =>
      if id == router(anchorId) then
        println("Toggling page event for id: " + id)
        router
          .get(id)
          .map { id =>
            println("Showing page for id: " + id)
            getDiv(id).style.setProperty("display", "block")
          }
          .getOrElse(
            println("No route found for id: " + id)
          )
      else
        println("Hiding page for id: " + id)
        router
          .get(id)
          .map { pageId =>
            println("Hiding page for id: " + pageId)
            getDiv(pageId).style.setProperty("display", "none")
          }
          .getOrElse(
            println("No route found for id: " + id)
          )
        // getDiv().style.setProperty("display", "none")

    }

  def toggleAnchorEvent(anchorId: String) =
    println(s"Toggle anchor event called: ${anchorId}")
    println(pageIds)
    if router.keySet.contains(anchorId) then togglePageEvent(anchorId)
    else println(s"Anchor event: Unregistered anchor id: ${anchorId}")
