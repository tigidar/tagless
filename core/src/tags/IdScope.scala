package tags

/** Re-export IdScope from htmlid module for backwards compatibility.
  *
  * Users can continue to import `tags.IdScope` - this export ensures existing
  * code continues to work.
  */
type IdScope[P <: String] = htmlid.IdScope[P]
