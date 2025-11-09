package tags

import zio.test.*
import zio.test.Assertion.*

import tags.defs.attrs.*
import tags.setters.*

object TagsSpec:

  val attrs = new HtmlAttrs {};

  def spec = suite("TagsSpec")(
    test("href html attribute") {
      val ref = attrs.href := "http://www.google.com"
      assert(ref)(equalTo(HtmlAttrSetter(attrs.href, "http://www.google.com")))
    },
  )
