package tags

import tags.i18n.{I18n, Lang}

/** Test helper for creating I18n values from String literals.
  *
  * Usage in tests:
  * {{{
  * import tags.TestI18n.{given, *}
  *
  * p("Hello".i18n)  // Creates I18n from String for testing
  * }}}
  */
object TestI18n:
  /** Default test language */
  val testLang: Lang = Lang("en")

  /** Given Lang for tests - import this to provide context */
  given Lang = testLang

  /** Extension to easily create I18n values from Strings for testing */
  extension (s: String)
    def i18n: I18n["test"] = I18n.of["test"](s, testLang -> s)
