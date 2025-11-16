package html.lib

import munit.*
import tagless.dsl.{given, *}
import tags.T.*
import colors.*
import menu.*

class HtmlLibTest extends FunSuite:

  val colorScheme = ColorTheme(
    Map(
      "light".theme -> "#ffffff".color,
      "dark".theme -> "#0b0f19".color
    )
  )

  test("todo"):
    assertEquals(1 + 1, 2)
