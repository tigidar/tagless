package ex

import tags.T
import tags.T.*
import html.lib.{headBlock, menuBlock}
import html.lib.oldmenu.*
import html.lib.colors.*
import tagless.dsl.{given, *}

// format: off
object Index:

  val colorScheme = ColorTheme(
    "light".theme -> "#ffffff".color,
    "dark".theme -> "#0b0f19".color
  )

  val menu = Menu(
    MenuKey("home") -> MenuItem(Title("Home"), Url("/")),
    MenuKey("todos") -> MenuItem(Title("Todos"), Url("/todos")),
    MenuKey("contact") -> MenuItem(Title("Contact"), Url("/contact")),
    MenuKey("about") -> MenuItem(Title("About"), Url("/about")),
  )

  val body = 
    ~ bodyTag
      >>^ menuBlock(menu)
      >> div(idAttr := "app")
      > div(idAttr := "welcome-content")
      >>^ Home.content
      >>^ About.content
      > scriptTag( tpe := "module", src := "/src/main.ts")

  val root = ~ htmlRootTag >> 
      headBlock("TODO App", colorScheme) + 
        body
 
