package ex

@main def run() =
  println(s"Generating new index: ${new java.util.Date().toString}")
  val dom = Index.root.resultTree.toHtml
  val pwd = os.pwd
  HtmlFmt.formatHtmlToFile(dom, pwd / "example" / "web" / "index.html")
