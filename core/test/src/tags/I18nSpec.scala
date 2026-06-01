package tags

import munit.FunSuite
import tags.i18n.*
import tags.dsl.*
import tags.html.*
import tags.attrs.*

// ============================================================
// Test language definitions
// ============================================================

object TestLang:
  val en = Lang("en")
  val de = Lang("de")
  val fr = Lang("fr")

// ============================================================
// Test text definitions — hierarchical scopes
// ============================================================

object TestTexts:
  import TestLang.*

  object home extends TextScope["home"]:
    val title = text["title"](en -> "Welcome", de -> "Willkommen")
    val content = text["content"](en -> "Hello World", de -> "Hallo Welt")

    object nav extends Sub["nav"]:
      val about = text["about"](en -> "About Us", de -> "Über uns")
      val contact = text["contact"](en -> "Contact", de -> "Kontakt")

  object about extends TextScope["about"]:
    val heading = text["heading"](en -> "About", de -> "Über", fr -> "À propos")
    val body = text["body"](en -> "Our story", de -> "Unsere Geschichte")

class I18nSpec extends FunSuite:
  import TestLang.*
  import TestTexts.*

  // ============================================================
  // Key path generation
  // ============================================================

  test("I18n key contains scope prefix and segment"):
    assertEquals(home.title.key, "home.title")

  test("I18n key for different segments"):
    assertEquals(home.content.key, "home.content")

  test("different scopes produce different key prefixes"):
    assertEquals(about.heading.key, "about.heading")
    assertEquals(about.body.key, "about.body")

  // ============================================================
  // Nested sub-scopes
  // ============================================================

  test("Sub scope prepends parent prefix with dot separator"):
    assertEquals(home.nav.about.key, "home.nav.about")
    assertEquals(home.nav.contact.key, "home.nav.contact")

  // ============================================================
  // Language resolution
  // ============================================================

  test("resolve returns translation for given language"):
    assertEquals(home.title.resolve(en), "Welcome")
    assertEquals(home.title.resolve(de), "Willkommen")

  test("resolve returns key when translation is missing"):
    assertEquals(home.title.resolve(fr), "home.title")

  test("resolve with fallback chain tries languages in order"):
    // fr not available for home.title, falls back to en
    assertEquals(home.title.resolve(fr, en), "Welcome")

  test("resolve with fallback uses first matching language"):
    // fr is available for about.heading
    assertEquals(about.heading.resolve(fr, en), "À propos")

  test("resolve with fallback returns key when none match"):
    val jp = Lang("jp")
    assertEquals(home.title.resolve(jp), "home.title")

  // ============================================================
  // Implicit Lang resolution
  // ============================================================

  test("apply resolves using implicit Lang"):
    given Lang = en
    assertEquals(home.title.apply, "Welcome")

  test("apply resolves German with implicit Lang"):
    given Lang = de
    assertEquals(home.title.apply, "Willkommen")

  test("switching implicit Lang changes resolution"):
    locally {
      given Lang = en
      assertEquals(home.nav.about.apply, "About Us")
    }
    locally {
      given Lang = de
      assertEquals(home.nav.about.apply, "Über uns")
    }

  // ============================================================
  // DSL integration — I18n with cursor operators
  // ============================================================

  test("I18n works with >> operator (add text child and descend)"):
    given Lang = en
    val result = (~div >> home.title).^.toHtml
    assertEquals(result, "<div>Welcome</div>")

  test("I18n works with >> operator in German"):
    given Lang = de
    val result = (~div >> home.title).^.toHtml
    assertEquals(result, "<div>Willkommen</div>")

  test("I18n works with >>^ operator (add text child and stay)"):
    given Lang = en
    val result = (~div >>^ home.title).toHtml
    assertEquals(result, "<div>Welcome</div>")

  test("I18n works with Tag.apply for text content"):
    given Lang = en
    val result = h1(home.title).toHtml
    assertEquals(result, "<h1>Welcome</h1>")

  test("I18n with Tag.apply in German"):
    given Lang = de
    val result = h1(home.title).toHtml
    assertEquals(result, "<h1>Willkommen</h1>")

  // ============================================================
  // DSL integration — nested scope text in cursor tree
  // ============================================================

  test("nested scope I18n in cursor tree"):
    given Lang = en
    val result = (
      ~div
        >> h1(home.title)
    ).^.toHtml
    assertEquals(result, "<div><h1>Welcome</h1></div>")

  test("multiple I18n texts in cursor tree"):
    given Lang = en
    val result = (
      ~div
        >>^ h1(home.title)
        >>^ p(home.content)
    ).toHtml
    assertEquals(result, "<div><h1>Welcome</h1><p>Hello World</p></div>")

  test("I18n with attributes in cursor tree"):
    given Lang = en
    val result = (
      ~(div | "main".cls)
        >> h1(home.title)
    ).^.toHtml
    assertEquals(result, """<div class="main"><h1>Welcome</h1></div>""")

  // ============================================================
  // Language switching — same tree, different language
  // ============================================================

  test("same tree structure renders differently per language"):
    def page(using Lang) =
      (~div
        >>^ h1(home.title)
        >>^ p(home.content)).toHtml

    assertEquals(
      page(using en),
      "<div><h1>Welcome</h1><p>Hello World</p></div>"
    )
    assertEquals(
      page(using de),
      "<div><h1>Willkommen</h1><p>Hallo Welt</p></div>"
    )

  // ============================================================
  // Type safety — different keys have different types
  // ============================================================

  test("keys from different scopes are distinct"):
    assertNotEquals(home.title.key, about.heading.key)

  test("keys from same scope with different segments are distinct"):
    assertNotEquals(home.title.key, home.content.key)

  test("nested keys include full path"):
    assert(home.nav.about.key.startsWith("home.nav."))
    assert(home.nav.contact.key.startsWith("home.nav."))

  // ============================================================
  // Lang equality
  // ============================================================

  test("Lang equality by code"):
    assertEquals(Lang("en"), Lang("en"))
    assertNotEquals(Lang("en"), Lang("de"))

  // ============================================================
  // Three-language support
  // ============================================================

  test("entry with three languages resolves all correctly"):
    assertEquals(about.heading.resolve(en), "About")
    assertEquals(about.heading.resolve(de), "Über")
    assertEquals(about.heading.resolve(fr), "À propos")

  // ============================================================
  // Tag.apply with varargs attributes
  // ============================================================

  test("Tag.apply with single attribute"):
    given Lang = en
    val result = h1(home.title, "main".cls).toHtml
    assertEquals(result, """<h1 class="main">Welcome</h1>""")

  test("Tag.apply with multiple attributes"):
    given Lang = en
    val result = h1(home.title, "heading".id, "main".cls).toHtml
    assertEquals(result, """<h1 id="heading" class="main">Welcome</h1>""")

  test("Tag.apply merges pipe attributes with varargs"):
    given Lang = en
    val result = (h1 | "base".cls)(home.title, "extra".id).toHtml
    assertEquals(result, """<h1 class="base" id="extra">Welcome</h1>""")
