package ex

import tags.T
import tags.T.*
import html.lib.{headBlock, menuBlock}
import html.lib.menu.*
import html.lib.colors.*
import html.lib.{isHidden, CssClass}
import tagless.dsl.{given, *}

import ex.PageMap.{NavButtons, Pages}

// format: off
object Index:

  val colorScheme = ColorTheme(
    "light".theme -> "#ffffff".color,
    "dark".theme -> "#0b0f19".color
  )

  val menu = Menu(
    NavButtons.homeId -> ("Home", "/"),
    NavButtons.todoId -> ("Todos", "/todos"),
    NavButtons.contactId -> ("Contact", "/contact"),
    NavButtons.aboutId -> ("About", "/about"),
  )

// format: off
  val body =
    ~ bodyTag
      >>^ menuBlock(menu)
       >> div(idAttr := "app")
          >> div(idAttr := Pages.welcomeId, cls := isHidden.value)
           >>^ Home.content
          <^ 1
          >> div(idAttr := Pages.aboutId, cls := isHidden.value) // descend down one step
            >>^ About.content // add the about content here without descending
          <^ 1 // move back up one level to app tag
          >> div(idAttr := Pages.todoListId, cls := isHidden.value) // add as child of app, i.e. a sibling to about
            >> div("some table will come here") // add the todo content as a child and descend into it
          <^ 2 // move back up 2 levels to app tag
            >> div(idAttr := Pages.contactId, cls := isHidden.value) // add another child of app
              >> div("contact us at") // add one child and descend into it
          <^ 2 // step two levels back to body
            >>^ ErrorPage.error(Pages.errorId) // We add the fragment also as a child of app but do not descend into it
          <^ 1 // move back up to body
            >> scriptTag( tpe := "module", src := "/src/main.ts") // adding script as a child of body

  val root = ~ htmlRootTag >>
      headBlock("TODO App", colorScheme) +
        body

