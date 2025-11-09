package tags.setters

import tags.keys.HtmlAttr

case class HtmlAttrSetter[Value](
  prop: HtmlAttr[Value],
  value: Value
) extends Setter[Value, String] {

  def domValue: String = prop.codec.encode(value)
}
