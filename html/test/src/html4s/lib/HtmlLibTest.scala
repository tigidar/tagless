package html.lib

import munit.*
import tagless.dsl.{given, *}
import tags.T.*
import colors.*
import menu.*

class HtmlLibTest extends FunSuite:

  test("Navbar navItems"):
    val menu = Menu(
      "home-id" -> ("Home", "/"),
      "todos-id" -> ("Todos", "/todo")
    )
    val menuFragment = menuBlock(menu).resultTree.toHtml

    val expected = """
<nav id="navbar" class="top-nav grid-2-3-1" aria-label="Primary" ><form id="theme-menu-form" class="theme-menu grid-start" ><label id="theme-label" class="theme-label" for="theme-select" ></label>
<select id="theme-select" class="theme-select" name="theme" ><option value="" >System</option>
<option value="light" >Light</option>
<option value="dark" >Dark</option>
<option value="night" >Night</option>
<option value="day" >Day</option>
<option value="xorian" >Xorian</option>
<option value="inverted-day" >Inverted Day</option>
</select>
</form>
<ul id="navbar-menu" ><li><a id="home-id" href="/" >Home</a>
</li>
<li><a id="todos-id" href="/todo" >Todos</a>
</li>
</ul>
</nav>
    """
    assertEquals(menuFragment, expected)
