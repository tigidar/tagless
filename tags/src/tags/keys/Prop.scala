package tags.keys

import tags.codecs.Codec
import tags.setters.PropSetter

class Prop[Value, DomValue](
  val domName: String,
  val codec: Codec[Value, DomValue]
) {

  def := (value: Value): PropSetter[Value, DomValue] = new PropSetter(this, value)
}
