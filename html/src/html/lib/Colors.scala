package html.lib

import scala.language.strictEquality
import scala.math.Ordering

object colors:

  // Opaque, zero-allocation aliases over String
  opaque type ThemeName = String
  opaque type ColorValue = String

  object ThemeName:
    inline def apply(s: String): ThemeName = s // no allocation
    extension (t: ThemeName) inline def value: String = t // unwrap
    // Useful typeclass instances
    given CanEqual[ThemeName, ThemeName] = CanEqual.derived
    given Ordering[ThemeName] = Ordering.by(_.value)

  object ColorValue:
    inline def apply(s: String): ColorValue = s
    extension (c: ColorValue) inline def value: String = c
    given CanEqual[ColorValue, ColorValue] = CanEqual.derived

  // Your case class
  final case class ColorTheme(scheme: Map[ThemeName, ColorValue])
  object ColorTheme:
    def apply(
        pairs: (ThemeName, ColorValue)*
    ): ColorTheme = ColorTheme(Map(pairs*))

  extension (s: String)
    def theme: ThemeName = ThemeName(s)
    def color: ColorValue = ColorValue(s)
