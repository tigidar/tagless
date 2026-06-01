package htmx

import munit.FunSuite
import htmx.hx.*
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.Attr

class HtmxSpec extends FunSuite:

  // ============================================================
  // Request Methods (TICKET-HX-001)
  // ============================================================

  test("hxGet creates correct attribute"):
    val attr = "/api/users".hxGet
    assertEquals(Attr.render(attr), """hx-get="/api/users"""")

  test("hxPost creates correct attribute"):
    val attr = "/api/submit".hxPost
    assertEquals(Attr.render(attr), """hx-post="/api/submit"""")

  test("hxPut creates correct attribute"):
    val attr = "/api/update".hxPut
    assertEquals(Attr.render(attr), """hx-put="/api/update"""")

  test("hxPatch creates correct attribute"):
    val attr = "/api/patch".hxPatch
    assertEquals(Attr.render(attr), """hx-patch="/api/patch"""")

  test("hxDelete creates correct attribute"):
    val attr = "/api/delete".hxDelete
    assertEquals(Attr.render(attr), """hx-delete="/api/delete"""")

  // ============================================================
  // Targeting (TICKET-HX-002)
  // ============================================================

  test("targeting attributes render correctly"):
    val html = (div | "#container".hxTarget | "outerHTML".hxSwap).toHtml
    assert(html.contains("""hx-target="#container""""))
    assert(html.contains("""hx-swap="outerHTML""""))

  test("hxSelect creates correct attribute"):
    val attr = "#result".hxSelect
    assertEquals(Attr.render(attr), """hx-select="#result"""")

  test("hxSelectOob creates correct attribute"):
    val attr = "#oob-item".hxSelectOob
    assertEquals(Attr.render(attr), """hx-select-oob="#oob-item"""")

  // ============================================================
  // Triggers and Confirmation (TICKET-HX-003)
  // ============================================================

  test("trigger with modifiers"):
    val attr = "keyup changed delay:500ms".hxTrigger
    assertEquals(
      Attr.render(attr),
      """hx-trigger="keyup changed delay:500ms""""
    )

  test("confirm message"):
    val attr = "Are you sure?".hxConfirm
    assertEquals(Attr.render(attr), """hx-confirm="Are you sure?"""")

  test("prompt message"):
    val attr = "Enter your name".hxPrompt
    assertEquals(Attr.render(attr), """hx-prompt="Enter your name"""")

  // ============================================================
  // Inclusion (TICKET-HX-004)
  // ============================================================

  test("hxInclude creates correct attribute"):
    val attr = "#other-form".hxInclude
    assertEquals(Attr.render(attr), """hx-include="#other-form"""")

  test("hxVals with JSON"):
    val attr = """{"id": 123}""".hxVals
    assertEquals(Attr.render(attr), """hx-vals="{"id": 123}"""")

  test("hxHeaders creates correct attribute"):
    val attr = """{"X-Custom": "value"}""".hxHeaders
    assertEquals(Attr.render(attr), """hx-headers="{"X-Custom": "value"}"""")

  // ============================================================
  // Loading Indicators (TICKET-HX-005)
  // ============================================================

  test("indicator selector"):
    val attr = "#spinner".hxIndicator
    assertEquals(Attr.render(attr), """hx-indicator="#spinner"""")

  test("disabled element selector"):
    val attr = "#submit-btn".hxDisabledElt
    assertEquals(Attr.render(attr), """hx-disabled-elt="#submit-btn"""")

  // ============================================================
  // Behavior (TICKET-HX-006)
  // ============================================================

  test("hxPushUrl creates correct attribute"):
    val attr = "true".hxPushUrl
    assertEquals(Attr.render(attr), """hx-push-url="true"""")

  test("hxSync creates correct attribute"):
    val attr = "closest form:abort".hxSync
    assertEquals(Attr.render(attr), """hx-sync="closest form:abort"""")

  test("hxBoost boolean attribute"):
    val html = (a | HxBool.hxBoost | "/about".href).toHtml
    assert(html.contains("""hx-boost="true""""))

  test("hxPreserve boolean attribute"):
    val html = (div | HxBool.hxPreserve).toHtml
    assert(html.contains("""hx-preserve="true""""))

  // ============================================================
  // Event Handlers (TICKET-HX-007)
  // ============================================================

  test("hxOn with custom event"):
    val attr = "console.log('clicked')".hxOn("click")
    assertEquals(Attr.render(attr), """hx-on:click="console.log('clicked')"""")

  test("hxOn with htmx event"):
    val attr = "showSpinner()".hxOn("htmx:beforeRequest")
    assertEquals(
      Attr.render(attr),
      """hx-on:htmx:beforeRequest="showSpinner()""""
    )

  // ============================================================
  // Complete Integration (TICKET-HX-008)
  // ============================================================

  test("complete HTMX button renders all attributes"):
    val html = (button
      | "/api/delete".hxDelete
      | "#item-list".hxTarget
      | "outerHTML".hxSwap
      | "Are you sure?".hxConfirm).toHtml

    assert(html.contains("""hx-delete="/api/delete""""))
    assert(html.contains("""hx-target="#item-list""""))
    assert(html.contains("""hx-swap="outerHTML""""))
    assert(html.contains("""hx-confirm="Are you sure?""""))

  test("search form with debounce"):
    val html =
      (~(form | "/search".hxGet | "keyup changed delay:300ms".hxTrigger)
        >>^ (input | "text".tpe | "q".name)).toHtml

    assert(html.contains("""hx-get="/search""""))
    assert(html.contains("""hx-trigger="keyup changed delay:300ms""""))

  test("infinite scroll pattern"):
    val html = (div
      | "/items?page=2".hxGet
      | "revealed".hxTrigger
      | "afterend".hxSwap).toHtml

    assert(html.contains("""hx-get="/items?page=2""""))
    assert(html.contains("""hx-trigger="revealed""""))
    assert(html.contains("""hx-swap="afterend""""))

  test("boosted navigation"):
    val html = (~(nav | HxBool.hxBoost)
      >>^ (a | "/home".href)
      >>^ (a | "/about".href)).toHtml

    assert(html.contains("""hx-boost="true""""))
    assert(html.contains("""href="/home""""))
    assert(html.contains("""href="/about""""))
