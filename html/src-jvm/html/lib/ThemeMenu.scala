package html.lib

import tags.T.*
import tagless.dsl.{given, *}
import dom.{Tree, Dom}

object ThemeMenu:
  val themeFormId: "theme-menu-form" = id["theme-menu-form"]
  val themeLabelId: "theme-label" = id["theme-label"]
  val themeSelectId: "theme-select" = id["theme-select"]

import ThemeMenu.*

/** Generate theme selector options from the typed Theme enum.
  * This ensures the HTML options match the CSS data-theme selectors.
  */
private def themeOptions: List[Tree[Dom]] =
  Theme.selectable.map { theme =>
    (~option(theme.displayName, value := theme.cssValue)).resultTree
  }

// format: off
def themeMenu =
  ~form(idAttr := themeFormId, cls := s"${themeMenuClass.value} ${gridStartClass.value}")
    >> label(
      idAttr := themeLabelId,
      cls := themeLabelClass.value,
      forId := themeSelectId
    )
      > select(
        idAttr := themeSelectId,
        cls := themeSelectClass.value,
        nameAttr := "theme"
      )
      >> themeOptions
