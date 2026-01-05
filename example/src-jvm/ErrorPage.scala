package ex

import tags.T
import tags.T.*

import tagless.Fragment
import html.lib.ErrorMessage
import html.lib.extensions.*

object ErrorPage:

  def error(id: String): Fragment = ErrorMessage().fragment(elementId = id)
