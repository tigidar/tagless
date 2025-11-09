package tags.defs.attrs

import tags.keys.TailwindAttr
import tags.codecs._

trait TailwindAttrs {

  def tailwindAttr[V](name: String, codec: Codec[V, String]): TailwindAttr[V] = new TailwindAttr(name, codec)

  lazy val tw = tailwindAttr[String]("data-tw", StringAsIsCodec)

}
