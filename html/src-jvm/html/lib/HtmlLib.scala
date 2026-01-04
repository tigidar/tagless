package html.lib

import menu.Menu
import colors.{ColorTheme}
import tags.T.*
import tags.Aria
import tagless.dsl.{given, *}
import tags.gen.HTMLLinkElement
import dom.Node
import dom.VoidType

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

def stylesheetLink(hrefValue: String): Node[VoidType, HTMLLinkElement] =
  linkTag(rel := "stylesheet", href := hrefValue)

def styleSheetsLinks(stylesheetPath: String, stylesheetPaths: String*): Vector[Node[VoidType, HTMLLinkElement]] =
  (stylesheetPath +: stylesheetPaths.toVector).map(stylesheetLink)

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
      > styleSheetsLinks(
          "/css/modern-normalize.css",
          "/css/flatten.css",
          "/css/layout.css",
          "/css/styles.css"
        )
      >^ colorSchemeMeta(colorScheme)


