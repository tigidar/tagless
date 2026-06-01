package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.{title as titleTag, *}
import tags.attrs.{title as titleAttr, *}
import tags.TestI18n.{given, *}

/** Test that builds the web/index.html structure using the new DSL.
  *
  * This verifies that the DSL can handle a real-world HTML document with:
  *   - Nested structures (html > head, html > body)
  *   - Multiple attributes
  *   - Mixed void and normal elements
  *   - Complex nested divs with classes and ids
  */
class IndexHtmlSpec extends FunSuite:

  test("can build head section"):
    val headContent = (
      head.toCursor
        >>^ (meta | Attr.Custom("charset", "UTF-8"))
        >>^ (meta | "viewport".name | "width=device-width, initial-scale=1.0"
          .attr("content"))
        >>^ titleTag("TODO App".i18n)
        >>^ (link | "icon".attr("rel") | "image/svg+xml".tpe | "/vite.svg".href)
        >>^ (link | "apple-touch-icon".attr(
          "rel"
        ) | "/apple-touch-icon.png".href)
        >>^ (link | "stylesheet".attr("rel") | "/css/flatten.css".href)
        >>^ (link | "stylesheet".attr("rel") | "/css/layout.css".href)
        >>^ (link | "stylesheet".attr("rel") | "/css/styles.css".href)
        >>^ (link | "stylesheet".attr("rel") | "/css/modern-normalize.css".href)
        >>^ (meta | "color-scheme".name | "light dark".attr("content"))
    ).toHtml

    assert(headContent.contains("<head>"), s"Missing <head> in: $headContent")
    assert(
      headContent.contains("""charset="UTF-8""""),
      s"Missing charset in: $headContent"
    )
    assert(
      headContent.contains("<title>TODO App</title>"),
      s"Missing title in: $headContent"
    )
    assert(
      headContent.contains("""rel="stylesheet""""),
      s"Missing stylesheet in: $headContent"
    )
    assert(headContent.contains("</head>"), s"Missing </head> in: $headContent")

  test("can build theme select options"):
    // Build the select with options
    val selectContent = (
      (select | "theme-select".id | "theme-select".cls | "theme".name).toCursor
        >>^ option("System".i18n)
        >>^ (option | "light".value)("Light".i18n)
        >>^ (option | "dark".value)("Dark".i18n)
        >>^ (option | "night".value)("Night".i18n)
        >>^ (option | "day".value)("Day".i18n)
        >>^ (option | "xorian".value)("Xorian".i18n)
        >>^ (option | "inverted-day".value)("Inverted Day".i18n)
    ).toHtml

    assert(
      selectContent.contains("""id="theme-select""""),
      s"Missing theme-select in: $selectContent"
    )
    assert(
      selectContent.contains("<option>System</option>"),
      s"Missing System option in: $selectContent"
    )
    assert(
      selectContent.contains("""value="light""""),
      s"Missing light value in: $selectContent"
    )

  test("can build navbar menu with links"):
    // Build each li separately to avoid ambiguous overloads
    val homeItem =
      (li.toCursor >> (a | "home-button".id | "/".href)("Home".i18n)).^.toHtml
    val todosItem = (li.toCursor >> (a | "todo-button".id | "/todos".href)(
      "Todos".i18n
    )).^.toHtml

    assert(
      homeItem.contains("""id="home-button""""),
      s"Missing home-button in: $homeItem"
    )
    assert(homeItem.contains("""href="/""""), s"Missing / href in: $homeItem")
    assert(homeItem.contains(">Home</a>"), s"Missing Home link in: $homeItem")
    assert(
      todosItem.contains("""href="/todos""""),
      s"Missing /todos href in: $todosItem"
    )

  test("can build simple navbar structure"):
    val navbar = (
      (nav | "navbar".id | "top-nav grid-2-3-1".cls | "Primary".attr(
        "aria-label"
      )).toCursor
        >>^ form
        >>^ ul
    ).toHtml

    assert(navbar.contains("""id="navbar""""), s"Missing navbar id in: $navbar")
    assert(
      navbar.contains("""class="top-nav grid-2-3-1""""),
      s"Missing navbar class in: $navbar"
    )
    assert(navbar.contains("<form>"), s"Missing form in: $navbar")
    assert(navbar.contains("<ul>"), s"Missing ul in: $navbar")

  test("can build welcome content section"):
    val welcomeContent = (
      (div | "welcome-content".id | "is-hidden".cls).toCursor
        >>^ h1("Welcome to the TODO App!".i18n)
        >>^ p(
          "This is a simple TODO application built with Scala.js and a custom markdown DSL.".i18n
        )
        >>^ h2("Greate features".i18n)
        >>^ (ul.toCursor
          >>^ li("Add new TODO items".i18n)
          >>^ li("Mark items as completed".i18n)
          >>^ li("Delete items".i18n))
        >>^ h2("Getting Started".i18n)
        >>^ p(
          "To get started, simply clone the repository and follow the instructions in the README file.".i18n
        )
    ).toHtml

    assert(
      welcomeContent.contains("""id="welcome-content""""),
      s"Missing welcome-content in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("""class="is-hidden""""),
      s"Missing is-hidden class in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("<h1>Welcome to the TODO App!</h1>"),
      s"Missing h1 in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("<li>Add new TODO items</li>"),
      s"Missing li in: $welcomeContent"
    )

  test("can build about content section"):
    val aboutContent = (
      (div | "about-content".id | "is-hidden".cls).toCursor
        >>^ h1("About the TODO App".i18n)
        >>^ p(
          "The TODO App is designed to help users manage their tasks efficiently.".i18n
        )
    ).toHtml

    assert(
      aboutContent.contains("""id="about-content""""),
      s"Missing about-content in: $aboutContent"
    )
    assert(
      aboutContent.contains("<h1>About the TODO App</h1>"),
      s"Missing h1 in: $aboutContent"
    )

  test("can build todo-list section"):
    val todoList = (
      (div | "todo-list".id | "is-hidden".cls).toCursor
        >>^ div("some table will come here".i18n)
    ).toHtml

    assert(
      todoList.contains("""id="todo-list""""),
      s"Missing todo-list in: $todoList"
    )

  test("can build contact section"):
    val contactContent = (
      (div | "contact-content".id | "is-hidden".cls).toCursor
        >>^ div("contact us at".i18n)
    ).toHtml

    assert(
      contactContent.contains("""id="contact-content""""),
      s"Missing contact-content in: $contactContent"
    )

  test("can build error page with nested structure"):
    // Build error page with h2 and p as siblings
    val errorPage = (
      (div | "error-page".id | "error-message is-hidden".cls | "polite".attr(
        "aria-live"
      )).toCursor
        >>^ (h2 | "error-title".id | "error-title".cls)("Error".i18n)
        >>^ (p | "error-message".id | "error-description".cls)(
          "An unexpected error has occurred.".i18n
        )
    ).toHtml

    assert(
      errorPage.contains("""id="error-page""""),
      s"Missing error-page in: $errorPage"
    )
    assert(
      errorPage.contains("""aria-live="polite""""),
      s"Missing aria-live in: $errorPage"
    )
    assert(
      errorPage.contains("""id="error-title""""),
      s"Missing error-title in: $errorPage"
    )
    assert(
      errorPage.contains("""id="error-message""""),
      s"Missing error-message in: $errorPage"
    )

  test("can build body section"):
    val bodyContent = (
      body.toCursor
        >>^ (nav | "navbar".id)
        >>^ (div | "app".id)
        >>^ (script | "module".tpe | "/src/main.ts".src)
    ).toHtml

    assert(bodyContent.contains("<body>"), s"Missing <body> in: $bodyContent")
    assert(
      bodyContent.contains("""id="navbar""""),
      s"Missing navbar in: $bodyContent"
    )
    assert(
      bodyContent.contains("""id="app""""),
      s"Missing app in: $bodyContent"
    )
    assert(
      bodyContent.contains("""type="module""""),
      s"Missing type=module in: $bodyContent"
    )
    assert(
      bodyContent.contains("""src="/src/main.ts""""),
      s"Missing src in: $bodyContent"
    )
    assert(bodyContent.contains("</body>"), s"Missing </body> in: $bodyContent")

  test("can build complete HTML document structure"):
    // Build head and body as separate nodes, then combine
    val headSection = (head.toCursor
      >>^ (meta | Attr.Custom("charset", "UTF-8"))
      >>^ titleTag("TODO App".i18n))

    val bodySection = (body.toCursor
      >>^ (nav | "navbar".id)
      >>^ (div | "app".id)
      >>^ (script | "module".tpe | "/src/main.ts".src))

    // Build the full document using >> to descend into children
    val document = (
      html.toCursor
        >>^ headSection
        >>^ bodySection
    ).toHtml

    assert(document.contains("<html>"), s"Missing <html> in: $document")
    assert(document.contains("<head>"), s"Missing <head> in: $document")
    assert(
      document.contains("""charset="UTF-8""""),
      s"Missing charset in: $document"
    )
    assert(
      document.contains("<title>TODO App</title>"),
      s"Missing title in: $document"
    )
    assert(document.contains("</head>"), s"Missing </head> in: $document")
    assert(document.contains("<body>"), s"Missing <body> in: $document")
    assert(
      document.contains("""id="navbar""""),
      s"Missing navbar in: $document"
    )
    assert(document.contains("""id="app""""), s"Missing app in: $document")
    assert(
      document.contains("""src="/src/main.ts""""),
      s"Missing src in: $document"
    )
    assert(document.contains("</body>"), s"Missing </body> in: $document")
    assert(document.contains("</html>"), s"Missing </html> in: $document")

  test("attributes are rendered correctly"):
    val elem = (div | "my-id".id | "class1 class2".cls | "custom-value".attr(
      "data-custom"
    )).toHtml
    assertEquals(
      elem,
      """<div id="my-id" class="class1 class2" data-custom="custom-value"></div>"""
    )

  test("void elements render self-closing"):
    val voidElems = (
      div.toCursor
        >>^ (meta | Attr.Custom("charset", "UTF-8"))
        >>^ (link | "stylesheet".attr("rel") | "/style.css".href)
        >>^ (input | "text".tpe | "username".name)
        >>^ (img | "/photo.jpg".src | "Photo".alt)
        >>^ br
        >>^ hr
    ).toHtml

    assert(
      voidElems.contains("""<meta charset="UTF-8" />"""),
      s"Missing meta in: $voidElems"
    )
    assert(
      voidElems.contains("""<link rel="stylesheet" href="/style.css" />"""),
      s"Missing link in: $voidElems"
    )
    assert(
      voidElems.contains("""<input type="text" name="username" />"""),
      s"Missing input in: $voidElems"
    )
    assert(
      voidElems.contains("""<img src="/photo.jpg" alt="Photo" />"""),
      s"Missing img in: $voidElems"
    )
    assert(voidElems.contains("<br />"), s"Missing br in: $voidElems")
    assert(voidElems.contains("<hr />"), s"Missing hr in: $voidElems")

  test("deeply nested structure"):
    // html > body > div#app > div.content > article > section > p
    val deep = (
      html.toCursor >>
        body >>
        (div | "app".id) >>
        (div | "content".cls) >>
        article >>
        section >>
        p("Deep content".i18n)
    )

    // Verify depth tracking works - we're at depth 7
    val result = deep.^^.^^.^^.toHtml

    assert(result.contains("<html>"), s"Missing html in: $result")
    assert(result.contains("<body>"), s"Missing body in: $result")
    assert(result.contains("""id="app""""), s"Missing app id in: $result")
    assert(
      result.contains("""class="content""""),
      s"Missing content class in: $result"
    )
    assert(result.contains("<article>"), s"Missing article in: $result")
    assert(result.contains("<section>"), s"Missing section in: $result")
    assert(result.contains("<p>Deep content</p>"), s"Missing p in: $result")

  // ============================================================
  // Tests using new Fragment helpers (demonstrates improved patterns)
  // ============================================================

  test("head section with Fragment.stylesheets"):
    // BEFORE: 4 lines of repetitive link elements
    // AFTER: 1 line with Fragment.stylesheets
    val headContent = (
      head.toCursor
        >>^ (meta | Attr.Custom("charset", "UTF-8"))
        >>^ titleTag("TODO App".i18n)
        >> Fragment.stylesheets("/css/")(
          "flatten",
          "layout",
          "styles",
          "modern-normalize"
        )
    ).toHtml

    assert(
      headContent.contains("""rel="stylesheet""""),
      s"Missing rel=stylesheet in: $headContent"
    )
    assert(
      headContent.contains("""href="/css/flatten.css""""),
      s"Missing flatten.css in: $headContent"
    )
    assert(
      headContent.contains("""href="/css/layout.css""""),
      s"Missing layout.css in: $headContent"
    )
    assert(
      headContent.contains("""href="/css/styles.css""""),
      s"Missing styles.css in: $headContent"
    )
    assert(
      headContent.contains("""href="/css/modern-normalize.css""""),
      s"Missing modern-normalize.css in: $headContent"
    )

  test("theme select with Fragment.options"):
    // BEFORE: 7 lines of repeated option elements
    // AFTER: 2 lines using Fragment helpers
    val selectContent = (
      (select | "theme-select".id | "theme".name).toCursor
        >> Fragment.simpleOptions("System".i18n)
        >> Fragment.options(
          ("light", "Light".i18n),
          ("dark", "Dark".i18n),
          ("night", "Night".i18n),
          ("day", "Day".i18n),
          ("xorian", "Xorian".i18n),
          ("inverted-day", "Inverted Day".i18n)
        )
    ).toHtml

    assert(
      selectContent.contains("<option>System</option>"),
      s"Missing System in: $selectContent"
    )
    assert(
      selectContent.contains("""value="light""""),
      s"Missing light in: $selectContent"
    )
    assert(
      selectContent.contains("""value="dark""""),
      s"Missing dark in: $selectContent"
    )
    assert(
      selectContent.contains(">Xorian</option>"),
      s"Missing Xorian in: $selectContent"
    )

  test("navbar menu with Fragment.navItems"):
    // BEFORE: Multiple lines building li > a structures manually
    // AFTER: Single Fragment.navItems call
    val navbar = (
      (ul | "navbar-menu".id).toCursor
        >> Fragment.navItems(
          "/" -> "Home".i18n,
          "/todos" -> "Todos".i18n,
          "/contact" -> "Contact".i18n,
          "/about" -> "About".i18n
        )
    ).toHtml

    assert(
      navbar.contains("""id="navbar-menu""""),
      s"Missing navbar-menu in: $navbar"
    )
    assert(navbar.contains("""href="/""""), s"Missing / href in: $navbar")
    assert(navbar.contains(">Home</a>"), s"Missing Home in: $navbar")
    assert(
      navbar.contains("""href="/about""""),
      s"Missing /about href in: $navbar"
    )

  test("feature list with Fragment.listItems"):
    // BEFORE: Multiple >>^ li("text") lines
    // AFTER: Single Fragment.listItems call
    val featureList = (
      ul.toCursor >> Fragment.listItems(
        "Add new TODO items".i18n,
        "Mark items as completed".i18n,
        "Delete items".i18n
      )
    ).toHtml

    assertEquals(
      featureList,
      "<ul><li>Add new TODO items</li><li>Mark items as completed</li><li>Delete items</li></ul>"
    )

  test("welcome section with Fragment.hiddenSection"):
    // BEFORE: (div | "welcome-content".id | "is-hidden".cls).toCursor
    // AFTER: Fragment.hiddenSection("welcome-content").toCursor
    val welcomeContent = (
      Fragment.hiddenSection("welcome-content").toCursor
        >>^ h1("Welcome to the TODO App!".i18n)
        >>^ p("This is a simple TODO application.".i18n)
        >> Fragment.listItems(
          "Feature 1".i18n,
          "Feature 2".i18n,
          "Feature 3".i18n
        )
    ).toHtml

    assert(
      welcomeContent.contains("""id="welcome-content""""),
      s"Missing welcome-content in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("""class="is-hidden""""),
      s"Missing is-hidden in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("<h1>Welcome to the TODO App!</h1>"),
      s"Missing h1 in: $welcomeContent"
    )
    assert(
      welcomeContent.contains("<li>Feature 1</li>"),
      s"Missing Feature 1 in: $welcomeContent"
    )

  test("Fragment.from for custom patterns"):
    // Build custom structures using Fragment.from
    val contentSections = List(
      ("welcome", "Welcome".i18n),
      ("about", "About".i18n),
      ("contact", "Contact".i18n)
    )
    val sections = Fragment.from(contentSections) { case (id, title) =>
      // Use the cursor directly (seal will be called by Fragment.from)
      Fragment.hiddenSection(id).toCursor >>^ h1(title)
    }
    val result = (div.toCursor >> sections).toHtml

    assert(result.contains("""id="welcome""""), s"Missing welcome in: $result")
    assert(result.contains("""id="about""""), s"Missing about in: $result")
    assert(result.contains("""id="contact""""), s"Missing contact in: $result")
    assert(
      result.contains("<h1>Welcome</h1>"),
      s"Missing Welcome h1 in: $result"
    )
