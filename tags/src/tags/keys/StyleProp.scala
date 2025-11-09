package tags.keys

//import tags.defs.styles.traits.GlobalKeywords
import tags.setters.StyleSetter

//import scala.scalajs.js.|

case class StyleProp[V](
  val domName: String
) 
//extends DerivedStyleBuilder[StyleSetter[_], DerivedStyleProp] with GlobalKeywords {
extends DerivedStyleBuilder[StyleSetter[?], DerivedStyleProp] {

  def := (value: V | String): StyleSetter[V] = StyleSetter(this, value.toString)

  def := (value: String): StyleSetter[V] = StyleSetter(this, value)

  override protected def styleSetter(value: String): StyleSetter[?] = this := value

  override protected def derivedStyle[A](encode: A => String): DerivedStyleProp[A] = {
    new DerivedStyleProp[A](this, encode)
  }
}
