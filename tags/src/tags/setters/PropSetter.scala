package tags.setters

import tags.keys.Prop

case class PropSetter[Value, DomValue](
  prop: Prop[Value, DomValue],
  value: Value
) extends Setter[Value, DomValue] {

  def domValue: DomValue = prop.codec.encode(value)
}
