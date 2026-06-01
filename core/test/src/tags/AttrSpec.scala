package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.TestI18n.{given, *}

class AttrSpec extends FunSuite:

  // ============================================================
  // New string attributes — rendering
  // ============================================================

  test("for attribute renders on label"):
    val result = (label | "email".forId).toHtml
    assertEquals(result, """<label for="email"></label>""")

  test("action attribute renders on form"):
    val result = (form | "/submit".action).toHtml
    assertEquals(result, """<form action="/submit"></form>""")

  test("method attribute renders on form"):
    val result = (form | "post".method).toHtml
    assertEquals(result, """<form method="post"></form>""")

  test("enctype attribute renders on form"):
    val result = (form | "multipart/form-data".enctype).toHtml
    assertEquals(result, """<form enctype="multipart/form-data"></form>""")

  test("target attribute renders on anchor"):
    val result = (a | "_blank".target).toHtml
    assertEquals(result, """<a target="_blank"></a>""")

  test("rel attribute renders on link"):
    val result = (link | "stylesheet".rel).toHtml
    assertEquals(result, """<link rel="stylesheet" />""")

  test("role attribute renders"):
    val result = (div | "navigation".role).toHtml
    assertEquals(result, """<div role="navigation"></div>""")

  test("width attribute renders"):
    val result = (img | "640".width).toHtml
    assertEquals(result, """<img width="640" />""")

  test("height attribute renders"):
    val result = (img | "480".height).toHtml
    assertEquals(result, """<img height="480" />""")

  test("lang attribute renders on html"):
    val result = (html | "en".lang).toHtml
    assertEquals(result, """<html lang="en"></html>""")

  test("dir attribute renders"):
    val result = (div | "rtl".dir).toHtml
    assertEquals(result, """<div dir="rtl"></div>""")

  test("tabindex attribute renders"):
    val result = (div | "0".tabindex).toHtml
    assertEquals(result, """<div tabindex="0"></div>""")

  test("colspan attribute renders on td"):
    val result = (td | "2".colspan).toHtml
    assertEquals(result, """<td colspan="2"></td>""")

  test("rowspan attribute renders on th"):
    val result = (th | "3".rowspan).toHtml
    assertEquals(result, """<th rowspan="3"></th>""")

  test("loading attribute renders on img"):
    val result = (img | "lazy".loading).toHtml
    assertEquals(result, """<img loading="lazy" />""")

  // ============================================================
  // New boolean attributes — rendering
  // ============================================================

  test("autoplay boolean attribute renders"):
    val result = (video | Attr.Autoplay).toHtml
    assertEquals(result, """<video autoplay></video>""")

  test("controls boolean attribute renders"):
    val result = (video | Attr.Controls).toHtml
    assertEquals(result, """<video controls></video>""")

  test("loop boolean attribute renders"):
    val result = (audio | Attr.Loop).toHtml
    assertEquals(result, """<audio loop></audio>""")

  test("muted boolean attribute renders"):
    val result = (video | Attr.Muted).toHtml
    assertEquals(result, """<video muted></video>""")

  test("open boolean attribute renders"):
    val result = (div | Attr.Open).toHtml
    assertEquals(result, """<div open></div>""")

  test("hidden boolean attribute renders"):
    val result = (div | Attr.Hidden).toHtml
    assertEquals(result, """<div hidden></div>""")

  test("autofocus boolean attribute renders"):
    val result = (input | Attr.Autofocus).toHtml
    assertEquals(result, """<input autofocus />""")

  test("multiple boolean attribute renders"):
    val result = (select | Attr.Multiple).toHtml
    assertEquals(result, """<select multiple></select>""")

  test("selected boolean attribute renders on option"):
    val result = (option | Attr.Selected).toHtml
    assertEquals(result, """<option selected></option>""")

  test("contenteditable boolean attribute renders"):
    val result = (div | Attr.Contenteditable).toHtml
    assertEquals(result, """<div contenteditable></div>""")

  test("draggable boolean attribute renders"):
    val result = (div | Attr.Draggable).toHtml
    assertEquals(result, """<div draggable></div>""")

  test("novalidate boolean attribute renders"):
    val result = (form | Attr.Novalidate).toHtml
    assertEquals(result, """<form novalidate></form>""")

  // ============================================================
  // ARIA attributes — rendering
  // ============================================================

  test("aria-label renders"):
    val result = (div | "Close".ariaLabel).toHtml
    assertEquals(result, """<div aria-label="Close"></div>""")

  test("aria-hidden renders"):
    val result = (div | "true".ariaHidden).toHtml
    assertEquals(result, """<div aria-hidden="true"></div>""")

  test("aria-describedby renders"):
    val result = (input | "help-text".ariaDescribedby).toHtml
    assertEquals(result, """<input aria-describedby="help-text" />""")

  test("aria-labelledby renders"):
    val result = (div | "title-id".ariaLabelledby).toHtml
    assertEquals(result, """<div aria-labelledby="title-id"></div>""")

  test("aria-expanded renders"):
    val result = (button | "false".ariaExpanded).toHtml
    assertEquals(result, """<button aria-expanded="false"></button>""")

  test("aria-controls renders"):
    val result = (button | "menu-panel".ariaControls).toHtml
    assertEquals(result, """<button aria-controls="menu-panel"></button>""")

  test("aria-live renders"):
    val result = (div | "polite".ariaLive).toHtml
    assertEquals(result, """<div aria-live="polite"></div>""")

  test("aria-current renders"):
    val result = (a | "page".ariaCurrent).toHtml
    assertEquals(result, """<a aria-current="page"></a>""")

  test("generic aria escape hatch renders"):
    val result = (div | "5".aria("valuemax")).toHtml
    assertEquals(result, """<div aria-valuemax="5"></div>""")

  // ============================================================
  // Combined attribute usage — realistic examples
  // ============================================================

  test("form with action, method, and novalidate"):
    val result =
      (form | "/api/submit".action | "post".method | Attr.Novalidate).toHtml
    assertEquals(
      result,
      """<form action="/api/submit" method="post" novalidate></form>"""
    )

  test("label with forId pointing to input"):
    val result = (~div
      >>^ (label | "email".forId)("Email:".i18n)
      >>^ (input | "email".id | "email".tpe)).toHtml
    assertEquals(
      result,
      """<div><label for="email">Email:</label><input id="email" type="email" /></div>"""
    )

  test("image with loading, width, height"):
    val result =
      (img | "photo.jpg".src | "Photo".alt | "lazy".loading | "640".width | "480".height).toHtml
    assertEquals(
      result,
      """<img src="photo.jpg" alt="Photo" loading="lazy" width="640" height="480" />"""
    )

  test("video with multiple boolean attributes"):
    val result =
      (video | Attr.Autoplay | Attr.Controls | Attr.Loop | Attr.Muted).toHtml
    assertEquals(result, """<video autoplay controls loop muted></video>""")

  test("accessible button with role and aria attributes"):
    val result =
      (div | "button".role | "Submit form".ariaLabel | "0".tabindex).toHtml
    assertEquals(
      result,
      """<div role="button" aria-label="Submit form" tabindex="0"></div>"""
    )

  test("table cell with colspan and rowspan"):
    val result = (td | "2".colspan | "3".rowspan).toHtml
    assertEquals(result, """<td colspan="2" rowspan="3"></td>""")

  test("html element with lang and dir"):
    val result = (html | "ar".lang | "rtl".dir).toHtml
    assertEquals(result, """<html lang="ar" dir="rtl"></html>""")

  test("link with rel and href"):
    val result = (a | "/about".href | "noopener".rel | "_blank".target).toHtml
    assertEquals(
      result,
      """<a href="/about" rel="noopener" target="_blank"></a>"""
    )

  test("select with multiple and autofocus"):
    val result = (select | Attr.Multiple | Attr.Autofocus).toHtml
    assertEquals(result, """<select multiple autofocus></select>""")

  // ============================================================
  // ADT round-trip — construct and match
  // ============================================================

  test("new string attribute ADT cases round-trip"):
    assertEquals(Attr.For("x"), Attr.For("x"))
    assertEquals(Attr.Action("/a"), Attr.Action("/a"))
    assertEquals(Attr.Method("post"), Attr.Method("post"))
    assertEquals(Attr.Enctype("e"), Attr.Enctype("e"))
    assertEquals(Attr.Target("_blank"), Attr.Target("_blank"))
    assertEquals(Attr.Rel("stylesheet"), Attr.Rel("stylesheet"))
    assertEquals(Attr.Role("button"), Attr.Role("button"))
    assertEquals(Attr.Width("100"), Attr.Width("100"))
    assertEquals(Attr.Height("50"), Attr.Height("50"))
    assertEquals(Attr.Lang("en"), Attr.Lang("en"))
    assertEquals(Attr.Dir("ltr"), Attr.Dir("ltr"))
    assertEquals(Attr.Tabindex("0"), Attr.Tabindex("0"))
    assertEquals(Attr.Colspan("2"), Attr.Colspan("2"))
    assertEquals(Attr.Rowspan("3"), Attr.Rowspan("3"))
    assertEquals(Attr.Loading("lazy"), Attr.Loading("lazy"))

  test("new boolean attribute ADT cases are singletons"):
    assert(Attr.Autoplay == Attr.Autoplay)
    assert(Attr.Controls == Attr.Controls)
    assert(Attr.Loop == Attr.Loop)
    assert(Attr.Muted == Attr.Muted)
    assert(Attr.Open == Attr.Open)
    assert(Attr.Hidden == Attr.Hidden)
    assert(Attr.Autofocus == Attr.Autofocus)
    assert(Attr.Multiple == Attr.Multiple)
    assert(Attr.Selected == Attr.Selected)
    assert(Attr.Contenteditable == Attr.Contenteditable)
    assert(Attr.Draggable == Attr.Draggable)
    assert(Attr.Novalidate == Attr.Novalidate)

  test("Aria ADT case round-trip"):
    assertEquals(Attr.Aria("label", "Close"), Attr.Aria("label", "Close"))
    assert(Attr.Aria("label", "Close") != Attr.Aria("hidden", "true"))
