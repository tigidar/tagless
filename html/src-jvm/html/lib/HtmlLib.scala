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


