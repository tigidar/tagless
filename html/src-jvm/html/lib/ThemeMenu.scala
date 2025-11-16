package html.lib

import tags.T.*
import tagless.dsl.{given, *}

object ThemeMenu:
  val themeFormId: "theme-menu-form" = id["theme-menu-form"]
  val themeLabelId = id["theme-label"]

import ThemeMenu.*
// format: off
def themeMenu =
  ~form(idAttr := themeFormId, cls := "theme-menu grid-start")
    >> label(
      idAttr := themeLabelId,
      cls := "theme-label",
      forId := "theme-select"
    )
      > select(
        idAttr := "theme-select",
        cls := "theme-select",
        nameAttr := "theme"
      )
      >> option("System", value := "")
       > option("Light", value := "light")
       > option("Dark", value := "dark")
       > option("Night", value := "night")
       > option("Day", value := "day")
       > option("Xorian", value := "xorian")
       > option("Inverted Day", value := "inverted-day")

