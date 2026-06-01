package tags

/** Predefined HTML tags.
  *
  * Normal tags can have children. Void tags are self-closing (no children
  * allowed).
  */
object html:

  // ============================================================
  // Document structure
  // ============================================================

  val html: Tag[Normal] = Tag.normal("html")
  val head: Tag[Normal] = Tag.normal("head")
  val body: Tag[Normal] = Tag.normal("body")
  val title: Tag[Normal] = Tag.normal("title")

  // ============================================================
  // Metadata (void elements)
  // ============================================================

  val meta: Tag[Void] = Tag.void("meta")
  val link: Tag[Void] = Tag.void("link")
  val base: Tag[Void] = Tag.void("base")

  // ============================================================
  // Sections
  // ============================================================

  val header: Tag[Normal] = Tag.normal("header")
  val footer: Tag[Normal] = Tag.normal("footer")
  val nav: Tag[Normal] = Tag.normal("nav")
  val main: Tag[Normal] = Tag.normal("main")
  val section: Tag[Normal] = Tag.normal("section")
  val article: Tag[Normal] = Tag.normal("article")
  val aside: Tag[Normal] = Tag.normal("aside")
  val address: Tag[Normal] = Tag.normal("address")

  // ============================================================
  // Grouping content
  // ============================================================

  val div: Tag[Normal] = Tag.normal("div")
  val p: Tag[Normal] = Tag.normal("p")
  val pre: Tag[Normal] = Tag.normal("pre")
  val blockquote: Tag[Normal] = Tag.normal("blockquote")
  val figure: Tag[Normal] = Tag.normal("figure")
  val figcaption: Tag[Normal] = Tag.normal("figcaption")

  // Lists
  val ul: Tag[Normal] = Tag.normal("ul")
  val ol: Tag[Normal] = Tag.normal("ol")
  val li: Tag[Normal] = Tag.normal("li")
  val dl: Tag[Normal] = Tag.normal("dl")
  val dt: Tag[Normal] = Tag.normal("dt")
  val dd: Tag[Normal] = Tag.normal("dd")

  // Void grouping
  val hr: Tag[Void] = Tag.void("hr")

  // ============================================================
  // Text-level semantics
  // ============================================================

  val span: Tag[Normal] = Tag.normal("span")
  val a: Tag[Normal] = Tag.normal("a")
  val em: Tag[Normal] = Tag.normal("em")
  val strong: Tag[Normal] = Tag.normal("strong")
  val small: Tag[Normal] = Tag.normal("small")
  val s: Tag[Normal] = Tag.normal("s")
  val cite: Tag[Normal] = Tag.normal("cite")
  val q: Tag[Normal] = Tag.normal("q")
  val dfn: Tag[Normal] = Tag.normal("dfn")
  val abbr: Tag[Normal] = Tag.normal("abbr")
  val time: Tag[Normal] = Tag.normal("time")
  val code: Tag[Normal] = Tag.normal("code")
  val `var`: Tag[Normal] = Tag.normal("var")
  val samp: Tag[Normal] = Tag.normal("samp")
  val kbd: Tag[Normal] = Tag.normal("kbd")
  val sub: Tag[Normal] = Tag.normal("sub")
  val sup: Tag[Normal] = Tag.normal("sup")
  val i: Tag[Normal] = Tag.normal("i")
  val b: Tag[Normal] = Tag.normal("b")
  val u: Tag[Normal] = Tag.normal("u")
  val mark: Tag[Normal] = Tag.normal("mark")
  val ruby: Tag[Normal] = Tag.normal("ruby")
  val rt: Tag[Normal] = Tag.normal("rt")
  val rp: Tag[Normal] = Tag.normal("rp")
  val bdi: Tag[Normal] = Tag.normal("bdi")
  val bdo: Tag[Normal] = Tag.normal("bdo")
  val data: Tag[Normal] = Tag.normal("data")

  // Void text elements
  val br: Tag[Void] = Tag.void("br")
  val wbr: Tag[Void] = Tag.void("wbr")

  // ============================================================
  // Headings
  // ============================================================

  val h1: Tag[Normal] = Tag.normal("h1")
  val h2: Tag[Normal] = Tag.normal("h2")
  val h3: Tag[Normal] = Tag.normal("h3")
  val h4: Tag[Normal] = Tag.normal("h4")
  val h5: Tag[Normal] = Tag.normal("h5")
  val h6: Tag[Normal] = Tag.normal("h6")
  val hgroup: Tag[Normal] = Tag.normal("hgroup")

  // ============================================================
  // Tables
  // ============================================================

  val table: Tag[Normal] = Tag.normal("table")
  val caption: Tag[Normal] = Tag.normal("caption")
  val colgroup: Tag[Normal] = Tag.normal("colgroup")
  val col: Tag[Void] = Tag.void("col")
  val thead: Tag[Normal] = Tag.normal("thead")
  val tbody: Tag[Normal] = Tag.normal("tbody")
  val tfoot: Tag[Normal] = Tag.normal("tfoot")
  val tr: Tag[Normal] = Tag.normal("tr")
  val th: Tag[Normal] = Tag.normal("th")
  val td: Tag[Normal] = Tag.normal("td")

  // ============================================================
  // Forms
  // ============================================================

  val form: Tag[Normal] = Tag.normal("form")
  val fieldset: Tag[Normal] = Tag.normal("fieldset")
  val legend: Tag[Normal] = Tag.normal("legend")
  val label: Tag[Normal] = Tag.normal("label")
  val button: Tag[Normal] = Tag.normal("button")
  val select: Tag[Normal] = Tag.normal("select")
  val datalist: Tag[Normal] = Tag.normal("datalist")
  val optgroup: Tag[Normal] = Tag.normal("optgroup")
  val option: Tag[Normal] = Tag.normal("option")
  val textarea: Tag[Normal] = Tag.normal("textarea")
  val output: Tag[Normal] = Tag.normal("output")
  val progress: Tag[Normal] = Tag.normal("progress")
  val meter: Tag[Normal] = Tag.normal("meter")

  // Void form elements
  val input: Tag[Void] = Tag.void("input")

  // ============================================================
  // Embedded content
  // ============================================================

  val picture: Tag[Normal] = Tag.normal("picture")
  val video: Tag[Normal] = Tag.normal("video")
  val audio: Tag[Normal] = Tag.normal("audio")
  val map: Tag[Normal] = Tag.normal("map")
  val `object`: Tag[Normal] = Tag.normal("object")
  val iframe: Tag[Normal] = Tag.normal("iframe")
  val canvas: Tag[Normal] = Tag.normal("canvas")

  // Void embedded elements
  val img: Tag[Void] = Tag.void("img")
  val embed: Tag[Void] = Tag.void("embed")
  val source: Tag[Void] = Tag.void("source")
  val track: Tag[Void] = Tag.void("track")
  val area: Tag[Void] = Tag.void("area")

  // ============================================================
  // Interactive elements
  // ============================================================

  val details: Tag[Normal] = Tag.normal("details")
  val summary: Tag[Normal] = Tag.normal("summary")
  val dialog: Tag[Normal] = Tag.normal("dialog")

  // ============================================================
  // Scripting
  // ============================================================

  val script: Tag[Normal] = Tag.normal("script")
  val noscript: Tag[Normal] = Tag.normal("noscript")
  val template: Tag[Normal] = Tag.normal("template")
  val slot: Tag[Normal] = Tag.normal("slot")
