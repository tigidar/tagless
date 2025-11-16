package ex

import org.scalajs.dom.{HTMLElement, HTMLDivElement}
import org.scalajs.dom
import scala.util.Try
import org.scalajs.dom.document
import com.raquo.airstream.core.{Observer, EventStream, Signal}
import com.raquo.airstream.ownership.{Owner, ManualOwner}

inline def get[A <: HTMLElement](id: String) =
  dom.document.getElementById(id).asInstanceOf[A]

inline def getDiv(id: String): HTMLDivElement =
  dom.document.getElementById(id).asInstanceOf[HTMLDivElement]

inline def hideDiv(id: String): Unit =
  getDiv(id).classList.add("is-hidden")

inline def showDiv(id: String): Unit =
  getDiv(id).classList.remove("is-hidden")

final case class SwitchEvent(id: String):

  def next(nextElementId: String): Option[SwitchEvent] =
    if nextElementId == "welcome-content" then None
    else
      Try {
        println("Switching content from: " + id + " to: " + nextElementId)
        hideDiv(id)
        showDiv(nextElementId)
        SwitchEvent(nextElementId)
      }.toOption

final case class Page(pageSwitch: SwitchEvent):
  self =>
  val router = PageMap.router

  private def updatePage(nextPage: PageType[PageState]): Unit =
    println("Updating page to: " + nextPage)

  def switchTo(anchorEvent: String): Page =
    if router.keySet.contains(anchorEvent) then
      val nextPage: PageType[PageState] = router(anchorEvent)
      pageSwitch.next(nextPage.elementId) match
        case Some(switchEvent) =>
          updatePage(nextPage)
          Page(switchEvent)
        case None =>
          val errorPage = PageMap.router(PageMap.Pages.errorId)
          pageSwitch.next(errorPage.elementId) match
            case Some(erroredSwitchEvent) =>
              updatePage(errorPage)
              Page(erroredSwitchEvent)
            case None =>
              // TODO: Fatal error, cannot switch to error page,
              // probably should show an alert somehow instead of throwing
              throw new Exception(
                "Failed to switch error page, going to error page."
              ) //
    else self

object Page:

  val initialPage =
    Page(SwitchEvent("welcome-content"))
