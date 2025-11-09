
package tags.gen

final case class SvgTag[El <: tags.gen.SVGElement] private (
  override val domName: String,
) extends Tag[El]

object SvgTag:

 def apply[El <: tags.gen.SVGElement](
    name: String
  ): SvgTag[El] = new SvgTag(name)
      
sealed trait SvgTags {

  lazy val a: SvgTag[SVGAElement] = SvgTag[SVGAElement]("a")


  lazy val altGlyph: SvgTag[SVGElement] = SvgTag[SVGElement]("altGlyph")


  lazy val altGlyphDef: SvgTag[SVGElement] = SvgTag[SVGElement]("altGlyphDef")


  lazy val altGlyphItem: SvgTag[SVGElement] = SvgTag[SVGElement]("altGlyphItem")


  lazy val animate: SvgTag[SVGElement] = SvgTag[SVGElement]("animate")


  lazy val animateMotion: SvgTag[SVGElement] = SvgTag[SVGElement]("animateMotion")


  lazy val animateTransform: SvgTag[SVGElement] = SvgTag[SVGElement]("animateTransform")


  lazy val circle: SvgTag[SVGCircleElement] = SvgTag[SVGCircleElement]("circle")


  lazy val clipPathTag: SvgTag[SVGClipPathElement] = SvgTag[SVGClipPathElement]("clipPath")


  lazy val colorProfileTag: SvgTag[SVGElement] = SvgTag[SVGElement]("color-profile")


  lazy val cursor: SvgTag[SVGElement] = SvgTag[SVGElement]("cursor")


  lazy val defs: SvgTag[SVGDefsElement] = SvgTag[SVGDefsElement]("defs")


  lazy val desc: SvgTag[SVGDescElement] = SvgTag[SVGDescElement]("desc")


  lazy val ellipse: SvgTag[SVGEllipseElement] = SvgTag[SVGEllipseElement]("ellipse")


  lazy val feBlend: SvgTag[SVGFEBlendElement] = SvgTag[SVGFEBlendElement]("feBlend")


  lazy val feColorMatrix: SvgTag[SVGFEColorMatrixElement] = SvgTag[SVGFEColorMatrixElement]("feColorMatrix")


  lazy val feComponentTransfer: SvgTag[SVGComponentTransferFunctionElement] = SvgTag[SVGComponentTransferFunctionElement]("feComponentTransfer")


  lazy val feComposite: SvgTag[SVGFECompositeElement] = SvgTag[SVGFECompositeElement]("feComposite")


  lazy val feConvolveMatrix: SvgTag[SVGFEConvolveMatrixElement] = SvgTag[SVGFEConvolveMatrixElement]("feConvolveMatrix")


  lazy val feDiffuseLighting: SvgTag[SVGFEDiffuseLightingElement] = SvgTag[SVGFEDiffuseLightingElement]("feDiffuseLighting")


  lazy val feDisplacementMap: SvgTag[SVGFEDisplacementMapElement] = SvgTag[SVGFEDisplacementMapElement]("feDisplacementMap")


  lazy val feDistantLighting: SvgTag[SVGFEDistantLightElement] = SvgTag[SVGFEDistantLightElement]("feDistantLighting")


  lazy val feFlood: SvgTag[SVGFEFloodElement] = SvgTag[SVGFEFloodElement]("feFlood")


  lazy val feFuncA: SvgTag[SVGFEFuncAElement] = SvgTag[SVGFEFuncAElement]("feFuncA")


  lazy val feFuncB: SvgTag[SVGFEFuncBElement] = SvgTag[SVGFEFuncBElement]("feFuncB")


  lazy val feFuncG: SvgTag[SVGFEFuncGElement] = SvgTag[SVGFEFuncGElement]("feFuncG")


  lazy val feFuncR: SvgTag[SVGFEFuncRElement] = SvgTag[SVGFEFuncRElement]("feFuncR")


  lazy val feGaussianBlur: SvgTag[SVGFEGaussianBlurElement] = SvgTag[SVGFEGaussianBlurElement]("feGaussianBlur")


  lazy val feImage: SvgTag[SVGFEImageElement] = SvgTag[SVGFEImageElement]("feImage")


  lazy val feMerge: SvgTag[SVGFEMergeElement] = SvgTag[SVGFEMergeElement]("feMerge")


  lazy val feMergeNode: SvgTag[SVGFEMergeNodeElement] = SvgTag[SVGFEMergeNodeElement]("feMergeNode")


  lazy val feMorphology: SvgTag[SVGFEMorphologyElement] = SvgTag[SVGFEMorphologyElement]("feMorphology")


  lazy val feOffset: SvgTag[SVGFEOffsetElement] = SvgTag[SVGFEOffsetElement]("feOffset")


  lazy val fePointLight: SvgTag[SVGFEPointLightElement] = SvgTag[SVGFEPointLightElement]("fePointLight")


  lazy val feSpecularLighting: SvgTag[SVGFESpecularLightingElement] = SvgTag[SVGFESpecularLightingElement]("feSpecularLighting")


  lazy val feSpotlight: SvgTag[SVGFESpotLightElement] = SvgTag[SVGFESpotLightElement]("feSpotlight")


  lazy val feTile: SvgTag[SVGFETileElement] = SvgTag[SVGFETileElement]("feTile")


  lazy val feTurbulence: SvgTag[SVGFETurbulenceElement] = SvgTag[SVGFETurbulenceElement]("feTurbulence")


  lazy val filter: SvgTag[SVGFilterElement] = SvgTag[SVGFilterElement]("filter")


  lazy val font: SvgTag[SVGElement] = SvgTag[SVGElement]("font")


  lazy val fontFace: SvgTag[SVGElement] = SvgTag[SVGElement]("font-face")


  lazy val fontFaceFormat: SvgTag[SVGElement] = SvgTag[SVGElement]("font-face-format")


  lazy val fontFaceName: SvgTag[SVGElement] = SvgTag[SVGElement]("font-face-name")


  lazy val fontFaceSrc: SvgTag[SVGElement] = SvgTag[SVGElement]("font-face-src")


  lazy val fontFaceUri: SvgTag[SVGElement] = SvgTag[SVGElement]("font-face-uri")


  lazy val foreignObject: SvgTag[SVGElement] = SvgTag[SVGElement]("foreignObject")


  lazy val g: SvgTag[SVGGElement] = SvgTag[SVGGElement]("g")


  lazy val glyph: SvgTag[SVGElement] = SvgTag[SVGElement]("glyph")


  lazy val glyphRef: SvgTag[SVGElement] = SvgTag[SVGElement]("glyphRef")


  lazy val hkern: SvgTag[SVGElement] = SvgTag[SVGElement]("hkern")


  lazy val image: SvgTag[SVGImageElement] = SvgTag[SVGImageElement]("image")


  lazy val line: SvgTag[SVGLineElement] = SvgTag[SVGLineElement]("line")


  lazy val linearGradient: SvgTag[SVGLinearGradientElement] = SvgTag[SVGLinearGradientElement]("linearGradient")


  lazy val marker: SvgTag[SVGMarkerElement] = SvgTag[SVGMarkerElement]("marker")


  lazy val mask: SvgTag[SVGMaskElement] = SvgTag[SVGMaskElement]("mask")


  lazy val metadata: SvgTag[SVGMetadataElement] = SvgTag[SVGMetadataElement]("metadata")


  lazy val missingGlyph: SvgTag[SVGElement] = SvgTag[SVGElement]("missing-glyph")


  lazy val mpath: SvgTag[SVGElement] = SvgTag[SVGElement]("mpath")


  lazy val path: SvgTag[SVGPathElement] = SvgTag[SVGPathElement]("path")


  lazy val pattern: SvgTag[SVGPatternElement] = SvgTag[SVGPatternElement]("pattern")


  lazy val polygon: SvgTag[SVGPolygonElement] = SvgTag[SVGPolygonElement]("polygon")


  lazy val polyline: SvgTag[SVGPolylineElement] = SvgTag[SVGPolylineElement]("polyline")


  lazy val radialGradient: SvgTag[SVGRadialGradientElement] = SvgTag[SVGRadialGradientElement]("radialGradient")


  lazy val rect: SvgTag[SVGRectElement] = SvgTag[SVGRectElement]("rect")


  lazy val set: SvgTag[SVGElement] = SvgTag[SVGElement]("set")


  lazy val stop: SvgTag[SVGStopElement] = SvgTag[SVGStopElement]("stop")


  lazy val svg: SvgTag[SVGSVGElement] = SvgTag[SVGSVGElement]("svg")


  lazy val switch: SvgTag[SVGSwitchElement] = SvgTag[SVGSwitchElement]("switch")


  lazy val symbol: SvgTag[SVGSymbolElement] = SvgTag[SVGSymbolElement]("symbol")


  lazy val text: SvgTag[SVGTextElement] = SvgTag[SVGTextElement]("text")


  lazy val textPath: SvgTag[SVGTextPathElement] = SvgTag[SVGTextPathElement]("textPath")


  lazy val titleTag: SvgTag[SVGTextPathElement] = SvgTag[SVGTextPathElement]("title")


  lazy val tref: SvgTag[SVGElement] = SvgTag[SVGElement]("tref")


  lazy val tspan: SvgTag[SVGTSpanElement] = SvgTag[SVGTSpanElement]("tspan")


  lazy val use: SvgTag[SVGUseElement] = SvgTag[SVGUseElement]("use")


  lazy val view: SvgTag[SVGViewElement] = SvgTag[SVGViewElement]("view")


  lazy val vkern: SvgTag[SVGElement] = SvgTag[SVGElement]("vkern")

}

