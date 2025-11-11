package html.lib

import menu.Menu
import colors.{ColorTheme}
import tags.T.*
import tags.Aria
import tagless.dsl.{given, *}

// format: off
def colorSchemeMeta(colorScheme: ColorTheme) =
  metaTag(
    nameAttr := "color-scheme",
    contentAttr := colorScheme.scheme.keys.mkString(" ")
  ) +: 
      colorScheme.scheme.map:
        case (theme, color) =>
          metaTag(
            nameAttr := s"theme-color",
            contentAttr := color.value,
            media := s"(prefers-color-scheme: ${theme.value})"
          )

def descriptionMeta(description: String) =
  metaTag(nameAttr := "description", contentAttr := description)

def keywordsMeta(keywords: String) =
  metaTag(nameAttr := "keywords", contentAttr := keywords)

def headBlock(titleText: String, colorScheme: ColorTheme) =
  ~ headTag
    >>  metaTag(charset := "UTF-8")
      > metaTag(
        nameAttr := "viewport",
        contentAttr := "width=device-width, initial-scale=1.0"
      )
      > titleTag(titleText)
      > linkTag(rel := "icon", tpe := "image/svg+xml", href := "/vite.svg")
      > linkTag(rel := "apple-touch-icon", href := "/apple-touch-icon.png")
      > linkTag(rel:= "stylesheet", href := "/css/modern-normalize.css")
      > linkTag(rel:= "stylesheet", href := "/css/flatten.css")
      > linkTag(rel:= "stylesheet", href := "/css/layout.css")
      > linkTag(rel:= "stylesheet", href := "/css/styles.css")
      > colorSchemeMeta(colorScheme)

def navItems(menu: Menu) =
  ~ ul(idAttr := "menu-items")
    >> menu.items.map:
       case (key, u) => 
         ~ li( idAttr := s"menu-${key}")
            >> a(u.title.value, href := u.url.value)

def themeMenu =
  ~ form(idAttr := "theme-menu", cls := "theme-menu grid-start")
      >> label(idAttr := "theme-label", cls := "theme-label", forId := "theme-select")
       > select( idAttr := "theme-select", cls := "theme-select", nameAttr := "theme")
         >> option("System", value := "")
          > option("Light", value := "light")
          > option("Dark", value := "dark")
          > option("Night", value := "night")
          > option("Day", value := "day")
          > option("Xorian", value := "xorian")
          > option("Inverted Day", value := "inverted-day")

def menuBlock(menu: Menu) =
  ~ navTag(
      idAttr := "menu", cls := "top-nav grid-1-3-1", Aria.label := "Primary"
    ) >> themeMenu + navItems(menu)

