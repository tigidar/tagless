package htmlid

import scala.compiletime.constValue
import scala.compiletime.ops.string.+

/** Typesafe HTML element ID with compile-time path tracking.
  *
  * Each `HtmlId` carries a singleton string literal type `S` representing its
  * full hyphen-separated path (e.g., `"home-nav"`). This enables:
  *   - Type-safe references: two IDs with different paths have different types
  *   - Pattern matching in event handlers via the singleton value
  *   - Hierarchical organization via `IdScope`
  *
  * ==Quick Start==
  *
  * Define your IDs in a hierarchy using `IdScope`:
  * {{{
  * import htmlid.{HtmlId, IdScope}
  *
  * object ids:
  *   object home extends IdScope["home"]:
  *     val nav     = id["nav"]      // HtmlId["home-nav"], value = "home-nav"
  *     val content = id["content"]  // HtmlId["home-content"]
  *
  *     object sidebar extends Sub["sidebar"]:
  *       val menu  = id["menu"]     // HtmlId["home-sidebar-menu"]
  *
  *   object about extends IdScope["about"]:
  *     val hero    = id["hero"]     // HtmlId["about-hero"]
  * }}}
  *
  * Use with the tagless DSL via the `|` operator:
  * {{{
  * import tags.dsl.*
  * import tags.html.*
  *
  * val page = (~div >> (nav | ids.home.nav)).^
  * // renders: <div><nav id="home-nav"></nav></div>
  * }}}
  *
  * Pattern match in event handlers:
  * {{{
  * elementId match
  *   case ids.home.nav     => handleNavClick()
  *   case ids.about.hero   => handleHeroScroll()
  * }}}
  */
opaque type HtmlId[S <: String] = String

object HtmlId:

  /** Create an HtmlId from a compile-time constant string type. */
  inline def apply[S <: String]: HtmlId[S] =
    constValue[S]

  extension [S <: String](htmlId: HtmlId[S])
    /** The underlying HTML id attribute value string. */
    def value: String = htmlId
