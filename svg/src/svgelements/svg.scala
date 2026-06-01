package svgelements

import tags.{Tag, Normal, Void}

/** SVG element definitions.
  *
  * Usage:
  * {{{
  * import svgelements.svg.*
  * import svgelements.svgattrs.*
  * import tags.dsl.*
  *
  * ~(svg | SvgNs.xmlns | SvgNs.viewBox(0, 0, 100, 100))
  *   >>^ (circle | "50".cx | "50".cy | "40".r | "blue".fill)
  * }}}
  *
  * ==Container Elements==
  * These can have children: svg, g, defs, symbol, marker, clipPath, mask,
  * pattern
  *
  * ==Shape Elements==
  * These are void (self-closing): circle, rect, ellipse, line, polyline,
  * polygon, path, use
  *
  * ==Text Elements==
  * These can have children: text, tspan, textPath
  *
  * ==Gradient Elements==
  * linearGradient and radialGradient can have children; stop is void
  */
object svg:

  // ============================================================
  // Container Elements (TICKET-SVG-001)
  // ============================================================

  /** SVG root element */
  val svg: Tag[Normal] = Tag.normal("svg")

  /** Group element for organizing other elements */
  val g: Tag[Normal] = Tag.normal("g")

  /** Container for reusable graphical elements */
  val defs: Tag[Normal] = Tag.normal("defs")

  /** Reusable graphical element (not directly rendered) */
  val symbol: Tag[Normal] = Tag.normal("symbol")

  /** Reference to another element (void - no children) */
  val use: Tag[Void] = Tag.void("use")

  /** Defines arrowheads or other markers */
  val marker: Tag[Normal] = Tag.normal("marker")

  /** Clipping path for elements */
  val clipPath: Tag[Normal] = Tag.normal("clipPath")

  /** Mask for elements */
  val mask: Tag[Normal] = Tag.normal("mask")

  /** Pattern for filling elements */
  val pattern: Tag[Normal] = Tag.normal("pattern")

  // ============================================================
  // Shape Elements (TICKET-SVG-002)
  // All shapes are void elements - no children allowed
  // ============================================================

  /** Circle shape */
  val circle: Tag[Void] = Tag.void("circle")

  /** Rectangle shape */
  val rect: Tag[Void] = Tag.void("rect")

  /** Ellipse shape */
  val ellipse: Tag[Void] = Tag.void("ellipse")

  /** Line segment */
  val line: Tag[Void] = Tag.void("line")

  /** Connected line segments (open shape) */
  val polyline: Tag[Void] = Tag.void("polyline")

  /** Connected line segments (closed shape) */
  val polygon: Tag[Void] = Tag.void("polygon")

  /** Path element for complex shapes */
  val path: Tag[Void] = Tag.void("path")

  // ============================================================
  // Text Elements (TICKET-SVG-003)
  // These can have children (text nodes, tspan)
  // ============================================================

  /** Text container element */
  val text: Tag[Normal] = Tag.normal("text")

  /** Text span within text element */
  val tspan: Tag[Normal] = Tag.normal("tspan")

  /** Text along a path */
  val textPath: Tag[Normal] = Tag.normal("textPath")

  // ============================================================
  // Gradient Elements (TICKET-SVG-004)
  // ============================================================

  /** Linear gradient definition */
  val linearGradient: Tag[Normal] = Tag.normal("linearGradient")

  /** Radial gradient definition */
  val radialGradient: Tag[Normal] = Tag.normal("radialGradient")

  /** Gradient color stop (void - no children) */
  val stop: Tag[Void] = Tag.void("stop")

  // ============================================================
  // Filter Elements
  // ============================================================

  /** Filter effects container */
  val filter: Tag[Normal] = Tag.normal("filter")

  /** Gaussian blur filter */
  val feGaussianBlur: Tag[Void] = Tag.void("feGaussianBlur")

  /** Color matrix filter */
  val feColorMatrix: Tag[Void] = Tag.void("feColorMatrix")

  /** Blend filter */
  val feBlend: Tag[Void] = Tag.void("feBlend")

  /** Offset filter */
  val feOffset: Tag[Void] = Tag.void("feOffset")

  /** Merge filter */
  val feMerge: Tag[Normal] = Tag.normal("feMerge")

  /** Merge node within feMerge */
  val feMergeNode: Tag[Void] = Tag.void("feMergeNode")

  /** Flood fill filter (fills with a solid color) */
  val feFlood: Tag[Void] = Tag.void("feFlood")

  /** Composite filter (combines images using Porter-Duff operations) */
  val feComposite: Tag[Void] = Tag.void("feComposite")

  /** Morphology filter (dilate/erode for outlines) */
  val feMorphology: Tag[Void] = Tag.void("feMorphology")

  // ============================================================
  // Animation Elements
  // ============================================================

  /** Animate attribute over time */
  val animate: Tag[Void] = Tag.void("animate")

  /** Animate transform over time */
  val animateTransform: Tag[Void] = Tag.void("animateTransform")

  /** Animate motion along a path */
  val animateMotion: Tag[Normal] = Tag.normal("animateMotion")

  /** Motion path for animateMotion */
  val mpath: Tag[Void] = Tag.void("mpath")

  /** Set attribute at specific time */
  val set: Tag[Void] = Tag.void("set")

  // ============================================================
  // Descriptive Elements
  // ============================================================

  /** Title for accessibility */
  val svgTitle: Tag[Normal] = Tag.normal("title")

  /** Description for accessibility */
  val desc: Tag[Normal] = Tag.normal("desc")

  /** Metadata container */
  val metadata: Tag[Normal] = Tag.normal("metadata")

  // ============================================================
  // Other Elements
  // ============================================================

  /** Image element */
  val image: Tag[Void] = Tag.void("image")

  /** Foreign object container (for HTML in SVG) */
  val foreignObject: Tag[Normal] = Tag.normal("foreignObject")

  /** Switch element for conditional processing */
  val switch: Tag[Normal] = Tag.normal("switch")
