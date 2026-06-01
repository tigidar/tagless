package tags.i18n

import scala.compiletime.constValue
import scala.compiletime.ops.string.+

/** Base class for defining hierarchical groups of typesafe translatable text.
  *
  * Extend `TextScope` with a type-level prefix string to create a namespace.
  * The prefix is automatically prepended to each text key, separated by dots.
  *
  * This is the i18n analogue of `IdScope` — it provides compile-time distinct
  * keys for every text entry in your application.
  *
  * ==Usage==
  *
  * {{{
  * import tags.i18n.*
  *
  * // Define your languages
  * object lang:
  *   val en = Lang("en")
  *   val de = Lang("de")
  *
  * // Define translatable text in scopes
  * object texts:
  *   import lang.*
  *
  *   object home extends TextScope["home"]:
  *     val title   = text["title"](en -> "Welcome", de -> "Willkommen")
  *     val content = text["content"](en -> "Hello!", de -> "Hallo!")
  *
  *     // Nested sub-scopes for deeper hierarchy
  *     object nav extends Sub["nav"]:
  *       val about   = text["about"](en -> "About", de -> "Über uns")
  *       val contact = text["contact"](en -> "Contact", de -> "Kontakt")
  *
  *   object about extends TextScope["about"]:
  *     val heading = text["heading"](en -> "About Us", de -> "Über uns")
  * }}}
  *
  * Uniqueness is guaranteed by the type system:
  *   - Each `text` call uses a distinct type parameter within its scope
  *   - The full key type (e.g., `"home.nav.about"`) is unique per entry
  *   - Two entries with different keys have incompatible types
  *   - Scala prevents duplicate `val` names within the same object
  *
  * @tparam P
  *   The singleton string literal type for this scope's prefix
  */
abstract class TextScope[P <: String]:

  /** Create a typesafe `I18n` text entry with translations.
    *
    * The resulting type is `I18n[P + "." + S]`, encoding the full key path.
    *
    * {{{
    * object home extends TextScope["home"]:
    *   val title = text["title"](en -> "Welcome", de -> "Willkommen")
    *   // type: I18n["home.title"], key: "home.title"
    * }}}
    *
    * @tparam S
    *   The singleton string literal for the text segment name
    * @param translations
    *   Language-to-text pairs
    * @return
    *   A typesafe I18n entry with the full key path as its type
    */
  inline def text[S <: String](
      translations: (Lang, String)*
  ): I18n[P + "." + S] =
    I18n[P + "." + S](constValue[P + "." + S], translations.toMap)

  /** Base class for nested sub-scopes within this scope.
    *
    * {{{
    * object home extends TextScope["home"]:
    *   object nav extends Sub["nav"]:
    *     val about = text["about"](en -> "About", de -> "Über uns")
    *     // type: I18n["home.nav.about"], key: "home.nav.about"
    * }}}
    *
    * @tparam S
    *   The singleton string literal for the sub-scope segment
    */
  abstract class Sub[S <: String] extends TextScope[P + "." + S]
