
package tags.gen

trait Tag[El <: tags.gen.Element] {
  val domName: String
}

sealed trait NodeType
sealed trait Parent extends NodeType
sealed trait Void extends NodeType

final case class HtmlTag[El <: tags.gen.Element, N <: NodeType] private (
  override val domName: String,
  void: Boolean
) extends Tag[El]

object HtmlTag:

 def apply[El <: tags.gen.Element, N <: tags.gen.NodeType](
    name: String, void: Boolean = false
  ): HtmlTag[El, N] = 
   new HtmlTag[El, N](name, void)

trait HtmlTags extends DocumentTags with SectionTags with GroupingTags with MiscTags with EmbedTags with TextTags with TableTags with FormTags
      
sealed trait DocumentTags {

  lazy val htmlRootTag: HtmlTag[HTMLHtmlElement, Parent ] = HtmlTag[HTMLHtmlElement, Parent]("html")


  lazy val headTag: HtmlTag[HTMLHeadElement, Parent ] = HtmlTag[HTMLHeadElement, Parent]("head")


  lazy val baseTag: HtmlTag[HTMLBaseElement, Void ] = HtmlTag[HTMLBaseElement, Void]("base", true)


  lazy val linkTag: HtmlTag[HTMLLinkElement, Void ] = HtmlTag[HTMLLinkElement, Void]("link", true)


  lazy val metaTag: HtmlTag[HTMLMetaElement, Void ] = HtmlTag[HTMLMetaElement, Void]("meta", true)


  lazy val scriptTag: HtmlTag[HTMLScriptElement, Parent ] = HtmlTag[HTMLScriptElement, Parent]("script")


  lazy val noScriptTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("noscript")

}

sealed trait SectionTags {

  lazy val bodyTag: HtmlTag[HTMLBodyElement, Parent ] = HtmlTag[HTMLBodyElement, Parent]("body")


  lazy val headerTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("header")


  lazy val footerTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("footer")


  lazy val h1: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h1")


  lazy val h2: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h2")


  lazy val h3: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h3")


  lazy val h4: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h4")


  lazy val h5: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h5")


  lazy val h6: HtmlTag[HTMLHeadingElement, Parent ] = HtmlTag[HTMLHeadingElement, Parent]("h6")

}

sealed trait GroupingTags {

  lazy val p: HtmlTag[HTMLParagraphElement, Parent ] = HtmlTag[HTMLParagraphElement, Parent]("p")


  lazy val hr: HtmlTag[HTMLHRElement, Void ] = HtmlTag[HTMLHRElement, Void]("hr", true)


  lazy val pre: HtmlTag[HTMLPreElement, Parent ] = HtmlTag[HTMLPreElement, Parent]("pre")


  lazy val blockQuote: HtmlTag[HTMLQuoteElement, Parent ] = HtmlTag[HTMLQuoteElement, Parent]("blockquote")


  lazy val ol: HtmlTag[HTMLOListElement, Parent ] = HtmlTag[HTMLOListElement, Parent]("ol")


  lazy val ul: HtmlTag[HTMLUListElement, Parent ] = HtmlTag[HTMLUListElement, Parent]("ul")


  lazy val li: HtmlTag[HTMLLIElement, Parent ] = HtmlTag[HTMLLIElement, Parent]("li")


  lazy val dl: HtmlTag[HTMLDListElement, Parent ] = HtmlTag[HTMLDListElement, Parent]("dl")


  lazy val dt: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("dt")


  lazy val dd: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("dd")


  lazy val figure: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("figure")


  lazy val figCaption: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("figcaption")


  lazy val div: HtmlTag[HTMLDivElement, Parent ] = HtmlTag[HTMLDivElement, Parent]("div")

}

sealed trait MiscTags {

  lazy val titleTag: HtmlTag[HTMLTitleElement, Parent ] = HtmlTag[HTMLTitleElement, Parent]("title")


  lazy val styleTag: HtmlTag[HTMLStyleElement, Parent ] = HtmlTag[HTMLStyleElement, Parent]("style")


  lazy val sectionTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("section")


  lazy val navTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("nav")


  lazy val articleTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("article")


  lazy val asideTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("aside")


  lazy val addressTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("address")


  lazy val mainTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("main")


  lazy val q: HtmlTag[HTMLQuoteElement, Parent ] = HtmlTag[HTMLQuoteElement, Parent]("q")


  lazy val dfn: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("dfn")


  lazy val abbr: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("abbr")


  lazy val dataTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("data")


  lazy val timeTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("time")


  lazy val varTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("var")


  lazy val samp: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("samp")


  lazy val kbd: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("kbd")


  lazy val mathTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("math")


  lazy val mark: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("mark")


  lazy val ruby: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("ruby")


  lazy val rt: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("rt")


  lazy val rp: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("rp")


  lazy val bdi: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("bdi")


  lazy val bdo: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("bdo")


  lazy val keyGenTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("keygen")


  lazy val outputTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("output")


  lazy val progressTag: HtmlTag[HTMLProgressElement, Parent ] = HtmlTag[HTMLProgressElement, Parent]("progress")


  lazy val meterTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("meter")


  lazy val detailsTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("details")


  lazy val summaryTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("summary")


  lazy val commandTag: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("command")


  lazy val menuTag: HtmlTag[HTMLMenuElement, Parent ] = HtmlTag[HTMLMenuElement, Parent]("menu")


  lazy val dialogTag: HtmlTag[HTMLDialogElement, Parent ] = HtmlTag[HTMLDialogElement, Parent]("dialog")

}

sealed trait EmbedTags {

  lazy val img: HtmlTag[HTMLImageElement, Void ] = HtmlTag[HTMLImageElement, Void]("img", true)


  lazy val iframe: HtmlTag[HTMLIFrameElement, Parent ] = HtmlTag[HTMLIFrameElement, Parent]("iframe")


  lazy val embedTag: HtmlTag[HTMLEmbedElement, Void ] = HtmlTag[HTMLEmbedElement, Void]("embed", true)


  lazy val objectTag: HtmlTag[HTMLObjectElement, Parent ] = HtmlTag[HTMLObjectElement, Parent]("object")


  lazy val paramTag: HtmlTag[HTMLParamElement, Void ] = HtmlTag[HTMLParamElement, Void]("param", true)


  lazy val videoTag: HtmlTag[HTMLVideoElement, Parent ] = HtmlTag[HTMLVideoElement, Parent]("video")


  lazy val audioTag: HtmlTag[HTMLAudioElement, Parent ] = HtmlTag[HTMLAudioElement, Parent]("audio")


  lazy val sourceTag: HtmlTag[HTMLSourceElement, Void ] = HtmlTag[HTMLSourceElement, Void]("source", true)


  lazy val trackTag: HtmlTag[HTMLTrackElement, Void ] = HtmlTag[HTMLTrackElement, Void]("track", true)


  lazy val canvasTag: HtmlTag[HTMLCanvasElement, Parent ] = HtmlTag[HTMLCanvasElement, Parent]("canvas")


  lazy val mapTag: HtmlTag[HTMLMapElement, Parent ] = HtmlTag[HTMLMapElement, Parent]("map")


  lazy val areaTag: HtmlTag[HTMLAreaElement, Void ] = HtmlTag[HTMLAreaElement, Void]("area", true)

}

sealed trait TextTags {

  lazy val a: HtmlTag[HTMLAnchorElement, Parent ] = HtmlTag[HTMLAnchorElement, Parent]("a")


  lazy val em: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("em")


  lazy val strong: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("strong")


  lazy val small: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("small")


  lazy val s: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("s")


  lazy val cite: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("cite")


  lazy val code: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("code")


  lazy val sub: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("sub")


  lazy val sup: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("sup")


  lazy val i: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("i")


  lazy val b: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("b")


  lazy val u: HtmlTag[HTMLElement, Parent ] = HtmlTag[HTMLElement, Parent]("u")


  lazy val span: HtmlTag[HTMLSpanElement, Parent ] = HtmlTag[HTMLSpanElement, Parent]("span")


  lazy val br: HtmlTag[HTMLBRElement, Void ] = HtmlTag[HTMLBRElement, Void]("br", true)


  lazy val wbr: HtmlTag[HTMLElement, Void ] = HtmlTag[HTMLElement, Void]("wbr", true)


  lazy val ins: HtmlTag[HTMLModElement, Parent ] = HtmlTag[HTMLModElement, Parent]("ins")


  lazy val del: HtmlTag[HTMLModElement, Parent ] = HtmlTag[HTMLModElement, Parent]("del")

}

sealed trait TableTags {

  lazy val table: HtmlTag[HTMLTableElement, Parent ] = HtmlTag[HTMLTableElement, Parent]("table")


  lazy val caption: HtmlTag[HTMLTableCaptionElement, Parent ] = HtmlTag[HTMLTableCaptionElement, Parent]("caption")


  lazy val colGroup: HtmlTag[HTMLTableColElement, Parent ] = HtmlTag[HTMLTableColElement, Parent]("colgroup")


  lazy val col: HtmlTag[HTMLTableColElement, Void ] = HtmlTag[HTMLTableColElement, Void]("col", true)


  lazy val tbody: HtmlTag[HTMLTableSectionElement, Parent ] = HtmlTag[HTMLTableSectionElement, Parent]("tbody")


  lazy val thead: HtmlTag[HTMLTableSectionElement, Parent ] = HtmlTag[HTMLTableSectionElement, Parent]("thead")


  lazy val tfoot: HtmlTag[HTMLTableSectionElement, Parent ] = HtmlTag[HTMLTableSectionElement, Parent]("tfoot")


  lazy val tr: HtmlTag[HTMLTableRowElement, Parent ] = HtmlTag[HTMLTableRowElement, Parent]("tr")


  lazy val td: HtmlTag[HTMLTableCellElement, Parent ] = HtmlTag[HTMLTableCellElement, Parent]("td")


  lazy val th: HtmlTag[HTMLTableCellElement, Parent ] = HtmlTag[HTMLTableCellElement, Parent]("th")

}

sealed trait FormTags {

  lazy val form: HtmlTag[HTMLFormElement, Parent ] = HtmlTag[HTMLFormElement, Parent]("form")


  lazy val fieldSet: HtmlTag[HTMLFieldSetElement, Parent ] = HtmlTag[HTMLFieldSetElement, Parent]("fieldset")


  lazy val legend: HtmlTag[HTMLLegendElement, Parent ] = HtmlTag[HTMLLegendElement, Parent]("legend")


  lazy val label: HtmlTag[HTMLLabelElement, Parent ] = HtmlTag[HTMLLabelElement, Parent]("label")


  lazy val input: HtmlTag[HTMLInputElement, Void ] = HtmlTag[HTMLInputElement, Void]("input", true)


  lazy val button: HtmlTag[HTMLButtonElement, Parent ] = HtmlTag[HTMLButtonElement, Parent]("button")


  lazy val select: HtmlTag[HTMLSelectElement, Parent ] = HtmlTag[HTMLSelectElement, Parent]("select")


  lazy val dataList: HtmlTag[HTMLDataListElement, Parent ] = HtmlTag[HTMLDataListElement, Parent]("datalist")


  lazy val optGroup: HtmlTag[HTMLOptGroupElement, Parent ] = HtmlTag[HTMLOptGroupElement, Parent]("optgroup")


  lazy val option: HtmlTag[HTMLOptionElement, Parent ] = HtmlTag[HTMLOptionElement, Parent]("option")


  lazy val textArea: HtmlTag[HTMLTextAreaElement, Parent ] = HtmlTag[HTMLTextAreaElement, Parent]("textarea")

}

