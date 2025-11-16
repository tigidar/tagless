package html.lib

import tags.T.*
import tags.Aria
import tagless.dsl.{given, *}
import menu.Menu

object Navbar:
  val navbarId: "navbar" = id["navbar"]
  val navbarMenuId: "navbar-menu" = id["navbar-menu"]

import Navbar.*

// format: off
def navItems(menu: Menu) =
  ~ ul(idAttr := navbarMenuId)
    >> menu.items.map:
       case (key, u) => 
         ~ li( )
            >> a(u.title.value, idAttr := s"${key}", href := u.url.value)


def menuBlock(menu: Menu) =
  ~ navTag(
      idAttr := navbarId, cls := "top-nav grid-2-3-1", Aria.label := "Primary"
    ) >> themeMenu + navItems(menu)
