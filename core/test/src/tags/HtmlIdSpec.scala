package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.HtmlId
import tags.HtmlId.{*, given}

// ============================================================
// Test ID definitions — hierarchical scopes
// ============================================================

object TestIds:
  object home extends IdScope["home"]:
    val nav = id["nav"]
    val content = id["content"]
    val footer = id["footer"]

    object sidebar extends Sub["sidebar"]:
      val menu = id["menu"]
      val links = id["links"]

  object about extends IdScope["about"]:
    val hero = id["hero"]
    val team = id["team"]

class HtmlIdSpec extends FunSuite:
  import TestIds.*

  // ============================================================
  // Basic HtmlId creation and value extraction
  // ============================================================

  test("HtmlId value contains scope prefix and segment"):
    assertEquals(home.nav.value, "home-nav")

  test("HtmlId value for different segments"):
    assertEquals(home.content.value, "home-content")
    assertEquals(home.footer.value, "home-footer")

  test("different scopes produce different prefixes"):
    assertEquals(about.hero.value, "about-hero")
    assertEquals(about.team.value, "about-team")

  // ============================================================
  // Nested sub-scopes
  // ============================================================

  test("Sub scope prepends parent prefix"):
    assertEquals(home.sidebar.menu.value, "home-sidebar-menu")
    assertEquals(home.sidebar.links.value, "home-sidebar-links")

  // ============================================================
  // DSL integration — HtmlId with Tag.| operator
  // ============================================================

  test("HtmlId converts to Attr.Id via toAttr"):
    assertEquals(home.nav.toAttr, Attr.Id("home-nav"))

  test("HtmlId works with Tag | operator via implicit conversion"):
    val result = (div | home.nav).toHtml
    assertEquals(result, """<div id="home-nav"></div>""")

  test("HtmlId works alongside other attributes"):
    val result = (div | home.content | "main".cls).toHtml
    assertEquals(result, """<div id="home-content" class="main"></div>""")

  test("nested scope ID renders correctly in HTML"):
    val result = (nav | home.sidebar.menu).toHtml
    assertEquals(result, """<nav id="home-sidebar-menu"></nav>""")

  // ============================================================
  // Integration with cursor DSL
  // ============================================================

  test("HtmlId in a cursor tree"):
    val result = (
      ~(div | home.content)
        >> (nav | home.sidebar.menu)
    ).^.toHtml
    assertEquals(
      result,
      """<div id="home-content"><nav id="home-sidebar-menu"></nav></div>"""
    )

  // ============================================================
  // Type safety — different IDs have different types
  // ============================================================

  test("IDs from different scopes are distinct values"):
    assertNotEquals(home.nav.value, about.hero.value)

  test("IDs from same scope with different segments are distinct"):
    assertNotEquals(home.nav.value, home.content.value)

  // ============================================================
  // Pattern matching on HtmlId values
  // ============================================================

  test("pattern match on HtmlId values"):
    val testId: String = home.nav.value
    val matched = testId match
      case s if s == home.nav.value     => "nav"
      case s if s == home.content.value => "content"
      case _                            => "unknown"
    assertEquals(matched, "nav")
