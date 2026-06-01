package svgelements

import tags.Attr

/** SVG attribute extensions.
  *
  * Usage:
  * {{{
  * import svgelements.svgattrs.*
  *
  * circle | "50".cx | "50".cy | "25".r
  * rect | "10".x | "10".y | "80".svgWidth | "40".svgHeight
  * path | "M10 10 L90 90".d | "red".fill
  * }}}
  *
  * ==Geometry Attributes==
  * Circle: cx, cy, r Rectangle: x, y, svgWidth, svgHeight, rx, ry Line: x1, y1,
  * x2, y2 Polyline/Polygon: points Path: d
  *
  * ==Presentation Attributes==
  * fill, stroke, strokeWidth, opacity, transform, etc.
  *
  * ==Reference Attributes==
  * href, xlinkHref, fillRef, clipPathRef, maskRef
  *
  * @see
  *   https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute
  */
object svgattrs:

  extension (s: String)

    // ============================================================
    // Geometry Attributes (TICKET-SVG-005)
    // ============================================================

    // --- Circle attributes ---

    /** Circle center X coordinate */
    def cx: Attr = Attr.Custom("cx", s)

    /** Circle center Y coordinate */
    def cy: Attr = Attr.Custom("cy", s)

    /** Circle/ellipse radius */
    def r: Attr = Attr.Custom("r", s)

    // --- Rectangle/general position ---

    /** X position */
    def x: Attr = Attr.Custom("x", s)

    /** Y position */
    def y: Attr = Attr.Custom("y", s)

    /** SVG width attribute (named svgWidth to avoid conflict with HTML width)
      */
    def svgWidth: Attr = Attr.Custom("width", s)

    /** SVG height attribute (named svgHeight to avoid conflict with HTML
      * height)
      */
    def svgHeight: Attr = Attr.Custom("height", s)

    /** Horizontal corner radius for rounded rectangles */
    def rx: Attr = Attr.Custom("rx", s)

    /** Vertical corner radius for rounded rectangles */
    def ry: Attr = Attr.Custom("ry", s)

    // --- Ellipse attributes ---

    /** Ellipse X radius (alternatives to r for ellipse) */
    // Note: rx and ry are already defined above

    // --- Line attributes ---

    /** Line start X coordinate */
    def x1: Attr = Attr.Custom("x1", s)

    /** Line start Y coordinate */
    def y1: Attr = Attr.Custom("y1", s)

    /** Line end X coordinate */
    def x2: Attr = Attr.Custom("x2", s)

    /** Line end Y coordinate */
    def y2: Attr = Attr.Custom("y2", s)

    // --- Polyline/polygon ---

    /** Points for polyline/polygon: "x1,y1 x2,y2 ..." */
    def points: Attr = Attr.Custom("points", s)

    // --- Path ---

    /** Path data commands */
    def d: Attr = Attr.Custom("d", s)

    /** Path length for accurate stroke-dasharray calculations */
    def pathLength: Attr = Attr.Custom("pathLength", s)

    // ============================================================
    // Presentation Attributes (TICKET-SVG-006)
    // ============================================================

    /** Fill color */
    def fill: Attr = Attr.Custom("fill", s)

    /** Stroke color */
    def stroke: Attr = Attr.Custom("stroke", s)

    /** Stroke width */
    def strokeWidth: Attr = Attr.Custom("stroke-width", s)

    /** Stroke line cap style (butt, round, square) */
    def strokeLinecap: Attr = Attr.Custom("stroke-linecap", s)

    /** Stroke line join style (miter, round, bevel) */
    def strokeLinejoin: Attr = Attr.Custom("stroke-linejoin", s)

    /** Stroke dash pattern */
    def strokeDasharray: Attr = Attr.Custom("stroke-dasharray", s)

    /** Stroke dash offset */
    def strokeDashoffset: Attr = Attr.Custom("stroke-dashoffset", s)

    /** Stroke miter limit */
    def strokeMiterlimit: Attr = Attr.Custom("stroke-miterlimit", s)

    /** Overall opacity */
    def opacity: Attr = Attr.Custom("opacity", s)

    /** Fill opacity */
    def fillOpacity: Attr = Attr.Custom("fill-opacity", s)

    /** Stroke opacity */
    def strokeOpacity: Attr = Attr.Custom("stroke-opacity", s)

    /** Fill rule (nonzero, evenodd) */
    def fillRule: Attr = Attr.Custom("fill-rule", s)

    /** Transform attribute (named svgTransform to avoid conflict with
      * String.transform)
      */
    def svgTransform: Attr = Attr.Custom("transform", s)

    /** Transform origin */
    def transformOrigin: Attr = Attr.Custom("transform-origin", s)

    // ============================================================
    // ViewBox and Namespace (TICKET-SVG-007)
    // ============================================================

    /** ViewBox attribute: "minX minY width height" */
    def viewBox: Attr = Attr.Custom("viewBox", s)

    /** Preserve aspect ratio */
    def preserveAspectRatio: Attr = Attr.Custom("preserveAspectRatio", s)

    // ============================================================
    // Gradient Attributes (TICKET-SVG-008)
    // ============================================================

    /** Gradient stop offset (0-1 or percentage) */
    def offset: Attr = Attr.Custom("offset", s)

    /** Gradient stop color */
    def stopColor: Attr = Attr.Custom("stop-color", s)

    /** Gradient stop opacity */
    def stopOpacity: Attr = Attr.Custom("stop-opacity", s)

    /** Gradient units (userSpaceOnUse, objectBoundingBox) */
    def gradientUnits: Attr = Attr.Custom("gradientUnits", s)

    /** Gradient transform */
    def gradientTransform: Attr = Attr.Custom("gradientTransform", s)

    /** Spread method for gradients (pad, reflect, repeat) */
    def spreadMethod: Attr = Attr.Custom("spreadMethod", s)

    // --- Radial gradient specific ---

    /** Radial gradient focal point X */
    def fx: Attr = Attr.Custom("fx", s)

    /** Radial gradient focal point Y */
    def fy: Attr = Attr.Custom("fy", s)

    /** Radial gradient focal radius */
    def fr: Attr = Attr.Custom("fr", s)

    // ============================================================
    // Reference Attributes (TICKET-SVG-008)
    // ============================================================

    /** SVG 2 href attribute for references */
    def svgHref: Attr = Attr.Custom("href", s)

    /** SVG 1.1 xlink:href attribute (legacy) */
    def xlinkHref: Attr = Attr.Custom("xlink:href", s)

    /** Reference fill using url() syntax */
    def fillRef: Attr = Attr.Custom("fill", s"url($s)")

    /** Reference clip-path using url() syntax */
    def clipPathRef: Attr = Attr.Custom("clip-path", s"url($s)")

    /** Reference mask using url() syntax */
    def maskRef: Attr = Attr.Custom("mask", s"url($s)")

    /** Reference filter using url() syntax */
    def filterRef: Attr = Attr.Custom("filter", s"url($s)")

    /** Reference marker-start using url() syntax */
    def markerStartRef: Attr = Attr.Custom("marker-start", s"url($s)")

    /** Reference marker-mid using url() syntax */
    def markerMidRef: Attr = Attr.Custom("marker-mid", s"url($s)")

    /** Reference marker-end using url() syntax */
    def markerEndRef: Attr = Attr.Custom("marker-end", s"url($s)")

    // ============================================================
    // Text Attributes
    // ============================================================

    /** Text anchor (start, middle, end) */
    def textAnchor: Attr = Attr.Custom("text-anchor", s)

    /** Dominant baseline */
    def dominantBaseline: Attr = Attr.Custom("dominant-baseline", s)

    /** Alignment baseline */
    def alignmentBaseline: Attr = Attr.Custom("alignment-baseline", s)

    /** Font family */
    def fontFamily: Attr = Attr.Custom("font-family", s)

    /** Font size */
    def fontSize: Attr = Attr.Custom("font-size", s)

    /** Font weight */
    def fontWeight: Attr = Attr.Custom("font-weight", s)

    /** Font style */
    def fontStyle: Attr = Attr.Custom("font-style", s)

    /** Letter spacing */
    def letterSpacing: Attr = Attr.Custom("letter-spacing", s)

    /** Word spacing */
    def wordSpacing: Attr = Attr.Custom("word-spacing", s)

    /** Text decoration */
    def textDecoration: Attr = Attr.Custom("text-decoration", s)

    /** Text length for textPath */
    def textLength: Attr = Attr.Custom("textLength", s)

    /** Length adjust method */
    def lengthAdjust: Attr = Attr.Custom("lengthAdjust", s)

    /** Start offset for textPath */
    def startOffset: Attr = Attr.Custom("startOffset", s)

    // ============================================================
    // Marker Attributes
    // ============================================================

    /** Marker width */
    def markerWidth: Attr = Attr.Custom("markerWidth", s)

    /** Marker height */
    def markerHeight: Attr = Attr.Custom("markerHeight", s)

    /** Marker reference X */
    def refX: Attr = Attr.Custom("refX", s)

    /** Marker reference Y */
    def refY: Attr = Attr.Custom("refY", s)

    /** Marker orientation */
    def orient: Attr = Attr.Custom("orient", s)

    /** Marker units */
    def markerUnits: Attr = Attr.Custom("markerUnits", s)

    // ============================================================
    // Pattern/ClipPath Attributes
    // ============================================================

    /** Pattern units */
    def patternUnits: Attr = Attr.Custom("patternUnits", s)

    /** Pattern content units */
    def patternContentUnits: Attr = Attr.Custom("patternContentUnits", s)

    /** Pattern transform */
    def patternTransform: Attr = Attr.Custom("patternTransform", s)

    /** Clip path units */
    def clipPathUnits: Attr = Attr.Custom("clipPathUnits", s)

    /** Mask units */
    def maskUnits: Attr = Attr.Custom("maskUnits", s)

    /** Mask content units */
    def maskContentUnits: Attr = Attr.Custom("maskContentUnits", s)

    // ============================================================
    // Filter Attributes
    // ============================================================

    /** Filter units */
    def filterUnits: Attr = Attr.Custom("filterUnits", s)

    /** Primitive units */
    def primitiveUnits: Attr = Attr.Custom("primitiveUnits", s)

    /** Filter result name */
    def result: Attr = Attr.Custom("result", s)

    /** Filter input */
    def in: Attr = Attr.Custom("in", s)

    /** Second filter input */
    def in2: Attr = Attr.Custom("in2", s)

    /** Standard deviation for blur */
    def stdDeviation: Attr = Attr.Custom("stdDeviation", s)

    /** Color matrix type */
    def colorMatrixType: Attr = Attr.Custom("type", s)

    /** Color matrix values */
    def values: Attr = Attr.Custom("values", s)

    /** Blend mode */
    def mode: Attr = Attr.Custom("mode", s)

    /** Offset dx */
    def dx: Attr = Attr.Custom("dx", s)

    /** Offset dy */
    def dy: Attr = Attr.Custom("dy", s)

    /** Flood fill color */
    def floodColor: Attr = Attr.Custom("flood-color", s)

    /** Flood fill opacity */
    def floodOpacity: Attr = Attr.Custom("flood-opacity", s)

    /** Composite/morphology operator */
    def operator: Attr = Attr.Custom("operator", s)

    /** Morphology radius */
    def radius: Attr = Attr.Custom("radius", s)

    // ============================================================
    // Animation Attributes
    // ============================================================

    /** Animation attribute name */
    def attributeName: Attr = Attr.Custom("attributeName", s)

    /** Animation from value */
    def from: Attr = Attr.Custom("from", s)

    /** Animation to value */
    def to: Attr = Attr.Custom("to", s)

    /** Animation by value */
    def by: Attr = Attr.Custom("by", s)

    /** Animation values */
    def animValues: Attr = Attr.Custom("values", s)

    /** Animation key times */
    def keyTimes: Attr = Attr.Custom("keyTimes", s)

    /** Animation key splines */
    def keySplines: Attr = Attr.Custom("keySplines", s)

    /** Animation duration */
    def dur: Attr = Attr.Custom("dur", s)

    /** Animation begin time */
    def begin: Attr = Attr.Custom("begin", s)

    /** Animation end time */
    def end: Attr = Attr.Custom("end", s)

    /** Animation repeat count */
    def repeatCount: Attr = Attr.Custom("repeatCount", s)

    /** Animation repeat duration */
    def repeatDur: Attr = Attr.Custom("repeatDur", s)

    /** Animation fill mode */
    def animFill: Attr = Attr.Custom("fill", s)

    /** Calc mode for animation */
    def calcMode: Attr = Attr.Custom("calcMode", s)

    /** Transform type for animateTransform */
    def transformType: Attr = Attr.Custom("type", s)

    /** Additive mode for animation */
    def additive: Attr = Attr.Custom("additive", s)

    /** Accumulate mode for animation */
    def accumulate: Attr = Attr.Custom("accumulate", s)

  /** SVG namespace constants and helpers */
  object SvgNs:
    /** SVG namespace URI */
    val ns = "http://www.w3.org/2000/svg"

    /** XLink namespace URI (for href in older SVG) */
    val xlinkNs = "http://www.w3.org/1999/xlink"

    /** xmlns attribute for SVG root element */
    val xmlns: Attr = Attr.Custom("xmlns", ns)

    /** xmlns:xlink attribute for XLink support */
    val xmlnsXlink: Attr = Attr.Custom("xmlns:xlink", xlinkNs)

    /** Create a viewBox from components */
    def viewBox(minX: Int, minY: Int, width: Int, height: Int): Attr =
      Attr.Custom("viewBox", s"$minX $minY $width $height")

    /** Create a viewBox from components (Double version) */
    def viewBox(
        minX: Double,
        minY: Double,
        width: Double,
        height: Double
    ): Attr =
      Attr.Custom("viewBox", s"$minX $minY $width $height")
