package tags

/** Tag definition with element kind phantom type.
  *
  * The K type parameter tracks whether this is a Normal element (can have
  * children) or a Void element (self-closing, no children). This enables
  * compile-time prevention of invalid operations like adding children to an
  * <img>.
  */
final case class Tag[K <: ElementKind](
    name: String,
    attrs: List[Attr] = Nil
):
  /** Add an attribute to this tag */
  def |(attr: Attr): Tag[K] =
    copy(attrs = attrs :+ attr)

  /** Add multiple attributes */
  def addAttrs(newAttrs: Attr*): Tag[K] =
    copy(attrs = attrs ++ newAttrs)

object Tag:
  /** Create a normal (can have children) tag */
  def normal(name: String): Tag[Normal] =
    Tag[Normal](name)

  /** Create a void (self-closing) tag */
  def void(name: String): Tag[Void] =
    Tag[Void](name)
