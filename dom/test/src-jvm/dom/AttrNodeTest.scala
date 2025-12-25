package dom

import munit.*

class AttrNodeTest extends FunSuite:

  test("HtmlAttr renders correctly"):
    val attr = AttrNode.HtmlAttr("class", "container")
    assertEquals(attr.render(), """class="container"""")

  test("Prop renders correctly"):
    val prop = AttrNode.Prop("checked", "true")
    assertEquals(prop.render(), """checked="true"""")

  test("Text renders its value"):
    val text = AttrNode.Text("Hello World")
    assertEquals(text.render(), "Hello World")

  test("Setter renders its value"):
    val setter = AttrNode.Setter("some-value")
    assertEquals(setter.render(), "some-value")

  test("Tailwind renders correctly"):
    val tw = AttrNode.Tailwind("class", "flex items-center")
    assertEquals(tw.render(), """class="flex items-center"""")

  test("Aria renders correctly"):
    val aria = AttrNode.Aria("aria-label", "Close button")
    assertEquals(aria.render(), """aria-label="Close button"""")
