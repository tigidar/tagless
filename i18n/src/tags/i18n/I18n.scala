package tags.i18n

import scala.compiletime.constValue
import scala.compiletime.ops.string.+

/** A typesafe, translatable text entry identified by a compile-time key.
  *
  * Each `I18n` carries:
  *   - A singleton string literal type `K` encoding the full dot-separated key
  *     path (e.g., `"home.title"`)
  *   - A `Map[Lang, String]` of translations keyed by language
  *
  * The key type `K` makes each text entry a distinct type at compile time,
  * enabling the compiler to catch misuse (e.g., passing the wrong text entry
  * where a specific one is expected).
  *
  * ==Quick Start==
  *
  * {{{
  * import tags.i18n.*
  *
  * // 1. Define your languages
  * object lang:
  *   val en = Lang("en")
  *   val de = Lang("de")
  *
  * // 2. Define your text entries in scopes
  * object texts:
  *   import lang.*
  *   object home extends TextScope["home"]:
  *     val title   = text["title"](en -> "Welcome", de -> "Willkommen")
  *     val subtitle = text["subtitle"](en -> "Hello!", de -> "Hallo!")
  *
  *     object nav extends Sub["nav"]:
  *       val about = text["about"](en -> "About Us", de -> "Über uns")
  *
  * // 3. Resolve text at runtime
  * given Lang = lang.en
  * val welcomeText: String = texts.home.title.apply  // "Welcome"
  *
  * // Or use the resolve method with an explicit language
  * texts.home.title.resolve(lang.de)  // "Willkommen"
  * }}}
  *
  * @tparam K
  *   Singleton string literal type encoding the full key path
  */
final case class I18n[K <: String](
    key: String,
    translations: Map[Lang, String]
):

  /** Resolve this text entry to the translation for the given language.
    *
    * @param lang
    *   The language to resolve to
    * @return
    *   The translated string, or the key if no translation exists
    */
  def resolve(lang: Lang): String =
    translations.getOrElse(lang, key)

  /** Resolve this text entry using an implicit language.
    *
    * {{{
    * given Lang = lang.en
    * val text: String = texts.home.title.apply  // "Welcome"
    * }}}
    */
  def apply(using lang: Lang): String =
    resolve(lang)

  /** Resolve with a fallback chain of languages.
    *
    * Tries each language in order until a translation is found. Falls back to
    * the key if none match.
    *
    * {{{
    * texts.home.title.resolve(lang.fr, lang.en)  // tries French, falls back to English
    * }}}
    */
  def resolve(first: Lang, rest: Lang*): String =
    (first +: rest)
      .collectFirst {
        case lang if translations.contains(lang) => translations(lang)
      }
      .getOrElse(key)

object I18n:

  /** Create an I18n entry directly (without a TextScope).
    *
    * Prefer using `TextScope` for organized, hierarchical text entries. This
    * factory is available for programmatic or one-off use cases.
    */
  def of[K <: String](key: String, translations: (Lang, String)*): I18n[K] =
    I18n[K](key, translations.toMap)
