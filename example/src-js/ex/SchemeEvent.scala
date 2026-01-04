package ex

import org.scalajs.dom
import html.lib.Theme

/** Type-safe theme switching using the Theme enum from html.lib.
  *
  * This demonstrates the typed state pattern for CSS theming:
  * 1. Theme values are constrained to valid CSS data-theme values
  * 2. Invalid theme strings are rejected at boundaries (fromCssValue)
  * 3. DOM manipulation uses the validated cssValue
  */
object SchemeEvent:

  /** Set the theme using a type-safe Theme value */
  def setTheme(theme: Theme): Unit =
    println(s"Setting theme to: $theme (css: ${theme.cssValue})")
    if theme == Theme.System then
      // Remove the attribute to use system preference
      dom.document.documentElement.removeAttribute("data-theme")
    else
      dom.document.documentElement.setAttribute("data-theme", theme.cssValue)

  /** Set the theme from a string value (for form inputs).
    * Falls back to System theme if the string is invalid.
    */
  def setScheme(value: String): Unit =
    println(s"SchemeEvent: received value '$value'")
    val theme = if value == null || value.isEmpty then Theme.System
    else Theme.fromCssValue(value).getOrElse {
      println(s"Warning: Unknown theme value '$value', using System")
      Theme.System
    }
    setTheme(theme)

  /** Get the current theme from the DOM */
  def currentTheme: Theme =
    val attr = dom.document.documentElement.getAttribute("data-theme")
    if attr == null || attr.isEmpty then Theme.System
    else Theme.fromCssValue(attr).getOrElse(Theme.System)
