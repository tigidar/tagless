package tags.i18n

/** A language tag identifying a natural language.
  *
  * `Lang` is a simple wrapper around a BCP 47 language tag string (e.g.,
  * `"en"`, `"de"`, `"fr"`). Define the languages your application supports as
  * `val` constants, then use them when defining translations in a `TextScope`.
  *
  * ==Quick Start==
  *
  * {{{
  * import tags.i18n.Lang
  *
  * // Define your app's supported languages
  * object lang:
  *   val en = Lang("en")
  *   val de = Lang("de")
  *   val fr = Lang("fr")
  * }}}
  *
  * @param code
  *   The BCP 47 language tag (e.g., `"en"`, `"de"`, `"fr"`)
  */
final case class Lang(code: String) derives CanEqual
