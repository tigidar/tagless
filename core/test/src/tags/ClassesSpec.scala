package tags

import munit.FunSuite
import tags.classes.*
import tags.dsl.*
import tags.html.*
import tags.attrs.*

class ClassesSpec extends FunSuite:

  // ============================================================
  // ClassList basic tests (TICKET-CSS-001)
  // ============================================================

  test("ClassList basic creation"):
    val cl = ClassList("a", "b", "c")
    assertEquals(cl.names, Vector("a", "b", "c"))

  test("ClassList add appends"):
    val cl = ClassList("a").add("b").add("c")
    assertEquals(cl.names, Vector("a", "b", "c"))

  test("ClassList add varargs"):
    val cl = ClassList("a").add("b", "c", "d")
    assertEquals(cl.names, Vector("a", "b", "c", "d"))

  test("ClassList toAttr joins with space"):
    val attr = ClassList("nav", "main").toAttr
    assertEquals(Attr.render(attr), """class="nav main"""")

  test("ClassList.empty is empty"):
    assertEquals(ClassList.empty.names, Vector.empty[String])
    assert(ClassList.empty.isEmpty)
    assert(!ClassList.empty.nonEmpty)

  test("ClassList nonEmpty/isEmpty"):
    val cl = ClassList("a")
    assert(cl.nonEmpty)
    assert(!cl.isEmpty)

  // ============================================================
  // Conditional class methods (TICKET-CSS-002)
  // ============================================================

  test("addWhen adds class when true"):
    val cl = ClassList("base").addWhen(true, "active")
    assertEquals(cl.names, Vector("base", "active"))

  test("addWhen skips class when false"):
    val cl = ClassList("base").addWhen(false, "active")
    assertEquals(cl.names, Vector("base"))

  test("addUnless adds class when false"):
    val cl = ClassList("base").addUnless(false, "visible")
    assertEquals(cl.names, Vector("base", "visible"))

  test("addUnless skips class when true"):
    val cl = ClassList("base").addUnless(true, "visible")
    assertEquals(cl.names, Vector("base"))

  test("addWhen varargs adds multiple classes when true"):
    val cl = ClassList("base").addWhen(true, "a", "b", "c")
    assertEquals(cl.names, Vector("base", "a", "b", "c"))

  test("addWhen varargs skips when false"):
    val cl = ClassList("base").addWhen(false, "a", "b", "c")
    assertEquals(cl.names, Vector("base"))

  test("chaining multiple conditionals"):
    val isActive = true
    val isHidden = false
    val cl = ClassList("btn")
      .addWhen(isActive, "active")
      .addUnless(isHidden, "visible")
    assertEquals(cl.names, Vector("btn", "active", "visible"))

  // ============================================================
  // classes() function (TICKET-CSS-003)
  // ============================================================

  test("classes creates Attr.Class"):
    val attr = classes("a", "b", "c")
    assertEquals(Attr.render(attr), """class="a b c"""")

  test("classes.opt filters None"):
    val attr = classes.opt(Some("a"), None, Some("c"))
    assertEquals(Attr.render(attr), """class="a c"""")

  test("classes.opt all None results in empty class"):
    val attr = classes.opt(None, None)
    assertEquals(Attr.render(attr), """class=""""")

  // ============================================================
  // String extensions (TICKET-CSS-004)
  // ============================================================

  test("clsWhen returns Some when true"):
    assertEquals("active".clsWhen(true), Some(Attr.Class("active")))

  test("clsWhen returns None when false"):
    assertEquals("active".clsWhen(false), None)

  test("clsUnless returns Some when false"):
    assertEquals("visible".clsUnless(false), Some(Attr.Class("visible")))

  test("clsUnless returns None when true"):
    assertEquals("visible".clsUnless(true), None)

  test("when returns Some(name) when true"):
    assertEquals("active".when(true), Some("active"))

  test("when returns None when false"):
    assertEquals("active".when(false), None)

  test("unless returns Some(name) when false"):
    assertEquals("visible".unless(false), Some("visible"))

  test("unless returns None when true"):
    assertEquals("visible".unless(true), None)

  // ============================================================
  // Tag integration (TICKET-CSS-005)
  // ============================================================

  test("|? adds attribute when Some"):
    val isActive = true
    val tag = div |? "active".clsWhen(isActive)
    assertEquals(tag.attrs, List(Attr.Class("active")))

  test("|? skips attribute when None"):
    val isActive = false
    val tag = div |? "active".clsWhen(isActive)
    assertEquals(tag.attrs, Nil)

  test("|? chaining with other operators"):
    val isActive = true
    val isHidden = false
    val tag =
      div | "base".cls |? "active".clsWhen(isActive) |? "hidden".clsWhen(
        isHidden
      )
    assertEquals(tag.attrs, List(Attr.Class("base"), Attr.Class("active")))

  // ============================================================
  // Class merging (TICKET-CSS-006)
  // ============================================================

  test("mergeClasses combines Class attrs"):
    val attrs = List(Attr.Class("a"), Attr.Id("x"), Attr.Class("b"))
    val merged = tags.mergeClasses(attrs)
    assertEquals(merged, List(Attr.Class("a b"), Attr.Id("x")))

  test("mergeClasses with no classes preserves other attrs"):
    val attrs = List(Attr.Id("x"), Attr.Href("/test"))
    val merged = tags.mergeClasses(attrs)
    assertEquals(merged, List(Attr.Id("x"), Attr.Href("/test")))

  test("mergeClasses with only classes"):
    val attrs = List(Attr.Class("a"), Attr.Class("b"), Attr.Class("c"))
    val merged = tags.mergeClasses(attrs)
    assertEquals(merged, List(Attr.Class("a b c")))

  test("tag.mergeClasses combines classes"):
    val tag = div | "a".cls | "x".id | "b".cls
    val merged = tag.mergeClasses
    assertEquals(merged.attrs.collect { case Attr.Class(v) => v }, List("a b"))

  // ============================================================
  // Integration tests
  // ============================================================

  test("ClassList builder pattern complete example"):
    val isActive = true
    val isHidden = false
    val attr = ClassList("btn")
      .add("primary")
      .addWhen(isActive, "active")
      .addUnless(isHidden, "visible")
      .toAttr
    assertEquals(Attr.render(attr), """class="btn primary active visible"""")

  test("complete example with div and ClassList"):
    val isSticky = true
    val html = (div | ClassList("nav")
      .add("horizontal")
      .addWhen(isSticky, "sticky")
      .toAttr).toHtml
    assertEquals(html, """<div class="nav horizontal sticky"></div>""")

  test("conditional classes with |? operator"):
    val isOpen = true
    val isDisabled = false
    val html =
      (div | "menu".cls |? "open".clsWhen(isOpen) |? "disabled".clsWhen(
        isDisabled
      )).mergeClasses.toHtml
    assertEquals(html, """<div class="menu open"></div>""")

  test("using classes.opt with when extension"):
    val hasShadow = true
    val isRounded = false
    val attr = classes.opt(
      "card".when(true),
      "shadow".when(hasShadow),
      "rounded".when(isRounded)
    )
    assertEquals(Attr.render(attr), """class="card shadow"""")
