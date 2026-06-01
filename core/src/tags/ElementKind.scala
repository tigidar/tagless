package tags

/** Phantom types for compile-time element kind tracking.
  *
  * Distinguishes elements that can have children (Normal) from self-closing
  * elements (Void). This prevents invalid operations like trying to add
  * children to an <img> or <br> tag.
  */
sealed trait ElementKind

/** Normal elements can have children (div, p, ul, span, etc.) */
sealed trait Normal extends ElementKind

/** Void elements are self-closing and cannot have children (img, br, input,
  * etc.)
  */
sealed trait Void extends ElementKind
