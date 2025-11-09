package tags.setters

import tags.keys.AriaAttr

case class AriaAttrSetter[Value](
    prop: AriaAttr[Value],
    value: Value
) extends Setter[Value, String] {

  def domValue: String = prop.codec.encode(value)
}
