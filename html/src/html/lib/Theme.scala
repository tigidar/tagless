package html.lib

/** Type-safe theme names that match CSS data-theme attribute values.
  *
  * This enum ensures that theme values used in both JVM (HTML generation)
  * and ScalaJS (runtime theme switching) are consistent with the CSS.
  *
  * CSS selectors like `:root[data-theme="night"]` must match these values.
  */
enum Theme(val cssValue: String, val displayName: String) derives CanEqual:
  case System extends Theme("", "System")
  case Light extends Theme("light", "Light")
  case Dark extends Theme("dark", "Dark")
  case Night extends Theme("night", "Night")
  case Day extends Theme("day", "Day")
  case Xorian extends Theme("xorian", "Xorian")
  case InvertedDay extends Theme("inverted-day", "Inverted Day")

object Theme:
  /** Parse a CSS value to a Theme, returning None for unknown values */
  def fromCssValue(s: String): Option[Theme] =
    if s == null || s.isEmpty then Some(System)
    else Theme.values.find(_.cssValue == s)

  /** All themes that can be selected by the user */
  val selectable: List[Theme] = List(
    System, Light, Dark, Night, Day, Xorian, InvertedDay
  )
