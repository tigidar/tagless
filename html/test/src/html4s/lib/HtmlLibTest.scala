package html.lib

import munit.*
import tagless.dsl.{given, *}
import tags.T.*
import colors.*
import menu.*
import os.exists

class HtmlLibTest extends FunSuite:

  // Use sourcecode.File() to get the actual source file location at compile time,
  // then navigate to project root (6 levels up from html/test/src/html4s/lib/HtmlLibTest.scala)
  private val thisFile = os.Path(sourcecode.File())
  private val projectRoot = thisFile / os.up / os.up / os.up / os.up / os.up / os.up
  private val webDir = projectRoot / "example" / "web"
  private val prettierBin = webDir / "node_modules" / ".bin" / "prettier"

  /** Format an HTML string using the project's local Prettier. */
  def formatHtml(html: String, timeoutMs: Int = 20_000): String =
    val cmd =
      if os.exists(prettierBin) then
        Seq(prettierBin.toString, "--parser", "html")
      else
        // Fallback to npx if local bin isn't present
        Seq("npx", "prettier", "--parser", "html")

    val res = os
      .proc(cmd)
      .call(
        cwd = webDir,
        stdin = html, // pipe HTML via stdin
        check = false, // we'll handle non-zero exit ourselves
        timeout = timeoutMs
      )

    if res.exitCode == 0 then res.out.text()
    else
      val err = res.err.text()
      throw new RuntimeException(s"Prettier failed (${res.exitCode}):\n$err")

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
    assertEquals(formatHtml(menuFragment), formatHtml(expected))

  // === Meta Tag Tests ===

  test("descriptionMeta generates correct meta tag"):
    val result = descriptionMeta("A test description")
    val html = (~result).resultTree.toHtml
    val expected = """
      <meta name="description" content="A test description" />
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("keywordsMeta generates correct meta tag"):
    val result = keywordsMeta("scala, html, dsl")
    val html = (~result).resultTree.toHtml
    val expected = """
      <meta name="keywords" content="scala, html, dsl" />
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("colorSchemeMeta generates meta tags for light and dark themes"):
    val colorScheme = ColorTheme(
      "light".theme -> "#ffffff".color,
      "dark".theme -> "#1a1a1a".color
    )
    val result = colorSchemeMeta(colorScheme)
    // colorSchemeMeta returns an Iterable of Nodes - join with newlines for proper formatting
    val html = result.map(node => (~node).resultTree.toHtml).mkString("\n")
    val expected = """
      <meta name="color-scheme" content="light dark" />
      <meta name="theme-color" content="#ffffff" media="(prefers-color-scheme: light)" />
      <meta name="theme-color" content="#1a1a1a" media="(prefers-color-scheme: dark)" />
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  // === Stylesheet Tests ===

  test("stylesheetLink creates single link element"):
    val result = stylesheetLink("/css/styles.css")
    val html = (~result).resultTree.toHtml
    val expected = """
      <link rel="stylesheet" href="/css/styles.css" />
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("styleSheetsLinks creates multiple link elements"):
    val result = styleSheetsLinks("/css/reset.css", "/css/main.css", "/css/theme.css")
    // Join with newlines for proper formatting
    val html = result.map(node => (~node).resultTree.toHtml).mkString("\n")
    val expected = """
      <link rel="stylesheet" href="/css/reset.css" />
      <link rel="stylesheet" href="/css/main.css" />
      <link rel="stylesheet" href="/css/theme.css" />
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  // === Head Block Tests ===

  test("headBlock generates complete head section"):
    val colorScheme = ColorTheme(
      "light".theme -> "#ffffff".color,
      "dark".theme -> "#000000".color
    )
    val result = headBlock("My Page Title", colorScheme)
    val html = result.resultTree.toHtml
    // Note: stylesheets appear in order based on DSL sibling operations
    val expected = """
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>My Page Title</title>
        <link rel="icon" type="image/svg+xml" href="/vite.svg" />
        <link rel="apple-touch-icon" href="/apple-touch-icon.png" />
        <link rel="stylesheet" href="/css/flatten.css" />
        <link rel="stylesheet" href="/css/layout.css" />
        <link rel="stylesheet" href="/css/styles.css" />
        <link rel="stylesheet" href="/css/modern-normalize.css" />
        <meta name="color-scheme" content="light dark" />
        <meta name="theme-color" content="#ffffff" media="(prefers-color-scheme: light)" />
        <meta name="theme-color" content="#000000" media="(prefers-color-scheme: dark)" />
      </head>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  // === Theme Menu Tests ===

  test("themeMenu generates theme selector form"):
    val result = themeMenu.resultTree.toHtml
    val expected = """
      <form id="theme-menu-form" class="theme-menu grid-start">
        <label id="theme-label" class="theme-label" for="theme-select"></label>
        <select id="theme-select" class="theme-select" name="theme">
          <option value="">System</option>
          <option value="light">Light</option>
          <option value="dark">Dark</option>
          <option value="night">Night</option>
          <option value="day">Day</option>
          <option value="xorian">Xorian</option>
          <option value="inverted-day">Inverted Day</option>
        </select>
      </form>
    """
    assertEquals(formatHtml(result), formatHtml(expected))

  // === Navigation Tests ===

  test("navItems generates navigation list from menu"):
    val menu = Menu(
      "about-id" -> ("About", "/about"),
      "contact-id" -> ("Contact", "/contact")
    )
    val result = navItems(menu).resultTree.toHtml
    val expected = """
      <ul id="navbar-menu">
        <li><a id="about-id" href="/about">About</a></li>
        <li><a id="contact-id" href="/contact">Contact</a></li>
      </ul>
    """
    assertEquals(formatHtml(result), formatHtml(expected))
