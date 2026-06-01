package htmlid

import scala.compiletime.constValue
import scala.compiletime.ops.string.+

/** Base class for defining hierarchical groups of typesafe HTML IDs.
  *
  * Extend `IdScope` with a type-level prefix string to create a namespace. The
  * prefix is automatically prepended to each ID separated by a hyphen.
  *
  * ==Usage==
  *
  * {{{
  * import htmlid.{HtmlId, IdScope}
  *
  * // Page-level scopes
  * object ids:
  *   object home extends IdScope["home"]:
  *     val nav     = id["nav"]      // HtmlId["home-nav"]
  *     val content = id["content"]  // HtmlId["home-content"]
  *
  *     // Nested sub-scopes for deeper hierarchy
  *     object sidebar extends Sub["sidebar"]:
  *       val menu  = id["menu"]     // HtmlId["home-sidebar-menu"]
  *       val links = id["links"]    // HtmlId["home-sidebar-links"]
  *
  *   object about extends IdScope["about"]:
  *     val hero   = id["hero"]      // HtmlId["about-hero"]
  *     val team   = id["about"]     // HtmlId["about-team"]
  * }}}
  *
  * Uniqueness is guaranteed by the type system:
  *   - Within a scope, each `id` call uses a distinct type parameter
  *   - The full path type (e.g., `"home-nav"`) is unique per ID
  *   - Two IDs with different paths have incompatible types
  *   - Scala prevents duplicate `val` names within the same object
  *
  * @tparam P
  *   The singleton string literal type for this scope's prefix
  */
abstract class IdScope[P <: String]:

  /** Create a typesafe `HtmlId` by joining the scope prefix and a segment.
    *
    * The resulting type is `HtmlId[P + "-" + S]`, encoding the full path.
    *
    * {{{
    * object home extends IdScope["home"]:
    *   val nav = id["nav"]  // type: HtmlId["home-nav"], value: "home-nav"
    * }}}
    */
  inline def id[S <: String]: HtmlId[P + "-" + S] =
    HtmlId[P + "-" + S]

  /** Base class for nested sub-scopes within this scope.
    *
    * {{{
    * object home extends IdScope["home"]:
    *   object sidebar extends Sub["sidebar"]:
    *     val menu = id["menu"]  // HtmlId["home-sidebar-menu"]
    * }}}
    *
    * @tparam S
    *   The singleton string literal for the sub-scope segment
    */
  abstract class Sub[S <: String] extends IdScope[P + "-" + S]
