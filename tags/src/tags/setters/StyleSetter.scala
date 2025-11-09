package tags.setters

import tags.keys.StyleProp

case class StyleSetter[V](prop: StyleProp[V], value: String)
    extends Setter[V, String]:

  def domValue: String =
    s"${prop.domName} = $value}"
