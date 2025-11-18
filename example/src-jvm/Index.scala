package ex

import tags.T
import tags.T.*
import html.lib.{headBlock, menuBlock}
import html.lib.menu.*
import html.lib.colors.*
import tagless.dsl.{given, *}

import ex.PageMap.{NavButtons, Pages}

// format: off
object Index:


  val colorScheme = ColorTheme(
    "light".theme -> "#ffffff".color,
    "dark".theme -> "#0b0f19".color
  )

  val menu = Menu(
    MenuKey(NavButtons.homeId) -> MenuItem(Title("Home"), Url("/")),
    MenuKey(NavButtons.todoId) -> MenuItem(Title("Todos"), Url("/todos")),
    MenuKey(NavButtons.contactId) -> MenuItem(Title("Contact"), Url("/contact")),
    MenuKey(NavButtons.aboutId) -> MenuItem(Title("About"), Url("/about")),
  )

// format: off
  val body = 
    ~ bodyTag
      >>^ menuBlock(menu)
       >> div(idAttr := "app")
          >> div(idAttr := Pages.welcomeId, cls := "is-hidden")
           >>^ Home.content
          <^ 1
          >> div(idAttr := Pages.aboutId, cls := "is-hidden")
            >>^ About.content
          <^ 1
      > scriptTag( tpe := "module", src := "/src/main.ts")

  val root = ~ htmlRootTag >> 
      headBlock("TODO App", colorScheme) + 
        body
 
