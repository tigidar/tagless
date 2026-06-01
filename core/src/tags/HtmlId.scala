package tags

/** Re-export HtmlId from htmlid module for backwards compatibility.
  *
  * Users can continue to import `tags.HtmlId` - this export ensures existing
  * code continues to work.
  */
type HtmlId[S <: String] = htmlid.HtmlId[S]

/** Companion object providing conversions for HtmlId integration with tags. */
object HtmlId:
  /** Create an HtmlId from a compile-time constant string type. */
  inline def apply[S <: String]: HtmlId[S] =
    htmlid.HtmlId[S]

  /** Implicit conversion so `HtmlId` can be used directly with `Tag.|` */
  given htmlIdToAttr[S <: String]: Conversion[HtmlId[S], Attr] with
    def apply(htmlId: HtmlId[S]): Attr = Attr.Id(htmlId.value)

  extension [S <: String](id: HtmlId[S])
    /** The underlying HTML id attribute value string. */
    def value: String = htmlid.HtmlId.value(id)

    /** Convert to an `Attr.Id` for use with the `|` operator on Tags. */
    def toAttr: Attr = Attr.Id(id.value)
