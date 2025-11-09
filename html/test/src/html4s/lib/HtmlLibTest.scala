package html4s.lib

import munit.*
import html4s.Dom.{given, *}
import tags.T.*
import html4s.*
import colors.*
import menu.*

class HtmlLibTest extends FunSuite:

  val colorScheme = ColorTheme(
    Map(
      "light".theme -> "#ffffff".color,
      "dark".theme -> "#0b0f19".color
    )
  )

  test("headBlock with color scheme"):

    val doc =
      headBlock("My Page", colorScheme) // >> bodyTag > h1("Welcome to My Page")

    val result = doc.build()
    val html = result.toHtmlSnippet()
    assertEquals(
      """<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>My Page</title><link rel="icon" type="image/svg+xml" href="/vite.svg"><link rel="apple-touch-icon" href="/apple-touch-icon.png"><meta name="color-scheme" content="light dark"><meta name="theme-color" content="#ffffff" media="(prefers-color-scheme: light)"><meta name="theme-color" content="#0b0f19" media="(prefers-color-scheme: dark)"></head>""",
      html
    )

  test("menuBlock"):

    val menu =
      Menu(
        MenuKey("home") -> MenuItem(Title("Home"), Url("/")),
        MenuKey("about") -> MenuItem(Title("About"), Url("/about")),
        MenuKey("contact") -> MenuItem(Title("Contact"), Url("/contact"))
      )

    val nav = menuBlock(menu)

    val result = nav.build()
    val html = result.toHtmlSnippet()
    assertEquals(
      html,
      """<nav><ul><li><a href="/">Home</a></li><li><a href="/about">About</a></li><li><a href="/contact">Contact</a></li></ul></nav>"""
    )
