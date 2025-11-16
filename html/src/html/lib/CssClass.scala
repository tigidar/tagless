package html.lib

/** Type-safe CSS class names shared between JVM and ScalaJS.
  *
  * Using opaque types provides compile-time safety without runtime overhead.
  * All CSS class manipulations should use these typed values instead of raw strings.
  */
object css:
  opaque type CssClass = String

  object CssClass:
    inline def apply(s: String): CssClass = s
    extension (c: CssClass) inline def value: String = c
    given CanEqual[CssClass, CssClass] = CanEqual.derived

  /** Standard visibility toggle class - used with display: none in CSS */
  val isHidden: CssClass = CssClass("is-hidden")

  /** Theme menu related classes */
  object ThemeClasses:
    val themeMenu: CssClass = CssClass("theme-menu")
    val themeLabel: CssClass = CssClass("theme-label")
    val themeSelect: CssClass = CssClass("theme-select")
    val gridStart: CssClass = CssClass("grid-start")

export css.{CssClass, isHidden}
export css.ThemeClasses.{themeMenu as themeMenuClass, themeLabel as themeLabelClass, themeSelect as themeSelectClass, gridStart as gridStartClass}
