package tags.setters

import tags.keys.TailwindAttr


case class TailwindAttrSetter[Value](
  prop: TailwindAttr[Value],
  value: Value
) extends Setter[Value, String] {

  def domValue: String = prop.codec.encode(value)
}
