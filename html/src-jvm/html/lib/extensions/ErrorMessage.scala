package html.lib.extensions

import tags.T.*
import tags.Aria
import tagless.dsl.{given, *}
import tagless.Fragment
import html.lib.isHidden

extension (e: html.lib.ErrorMessage)
  //format: off
  def fragment(
      elementId: String = "error-message-container",
      titleId: String = "error-title",
      messageId: String = "error-message"
  ): Fragment =
    ~ div(
      idAttr := elementId,
      cls := s"error-message ${isHidden.value}",
      Aria.live := "polite"
    )
      >> h2(e.title, idAttr := titleId, cls := "error-title")
      >> p(
        e.message,
        idAttr := messageId,
        cls := "error-description"
      )
