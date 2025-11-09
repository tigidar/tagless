package tags.keys

import tags.codecs.Codec
import tags.setters.TailwindAttrSetter

class TailwindAttr[V](
  override val domName: String,
  override val codec: Codec[V, String]
) extends Attr[V] {

  def := (value: V): TailwindAttrSetter[V] = TailwindAttrSetter(this, value)
}
