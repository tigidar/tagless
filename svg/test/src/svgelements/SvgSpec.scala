package svgelements

import munit.FunSuite
import svgelements.svg.*
import svgelements.svgattrs.*
import tags.dsl.*
import tags.attrs.*
import tags.Attr

class SvgSpec extends FunSuite:

  // ============================================================
  // Container Elements (TICKET-SVG-001)
  // ============================================================

  test("svg container element defined"):
    assertEquals(svg.name, "svg")

  test("use is void element"):
    assertEquals(use.name, "use")
    assertEquals(use.toHtml, "<use />")

  test("svg can have children"):
    val html = (~svg >>^ g).toHtml
    assert(html.contains("<svg><g></g></svg>"))

  // ============================================================
  // Shape Elements (TICKET-SVG-002)
  // ============================================================

  test("circle renders as void element"):
    val html = circle.toHtml
    assertEquals(html, "<circle />")

  test("path renders as void element"):
    val html = path.toHtml
    assertEquals(html, "<path />")

  test("rect renders as void element"):
    val html = rect.toHtml
    assertEquals(html, "<rect />")

  test("line renders as void element"):
    val html = line.toHtml
    assertEquals(html, "<line />")

  test("polyline renders as void element"):
    val html = polyline.toHtml
    assertEquals(html, "<polyline />")

  test("polygon renders as void element"):
    val html = polygon.toHtml
    assertEquals(html, "<polygon />")

  test("ellipse renders as void element"):
    val html = ellipse.toHtml
    assertEquals(html, "<ellipse />")

  // ============================================================
  // Text Elements (TICKET-SVG-003)
  // ============================================================

  test("text can have children"):
    val html = (~text >>^ tspan).toHtml
    assert(html.contains("<text><tspan></tspan></text>"))

  test("textPath can have text content"):
    val html = (~textPath >>^ tags.Node.Text("Hello")).toHtml
    assert(html.contains("<textPath>Hello</textPath>"))

  // ============================================================
  // Gradient Elements (TICKET-SVG-004)
  // ============================================================

  test("linearGradient can contain stops"):
    val html = (~linearGradient >>^ stop >>^ stop).toHtml
    assert(html.contains("<linearGradient><stop /><stop /></linearGradient>"))

  test("radialGradient can contain stops"):
    val html = (~radialGradient >>^ stop).toHtml
    assert(html.contains("<radialGradient><stop /></radialGradient>"))

  test("stop renders as void element"):
    val html = stop.toHtml
    assertEquals(html, "<stop />")

  // ============================================================
  // Geometry Attributes (TICKET-SVG-005)
  // ============================================================

  test("circle attributes render correctly"):
    val html = (circle | "50".cx | "50".cy | "25".r).toHtml
    assert(html.contains("""cx="50""""))
    assert(html.contains("""cy="50""""))
    assert(html.contains("""r="25""""))

  test("rect attributes render correctly"):
    val html = (rect | "10".x | "20".y | "100".svgWidth | "50".svgHeight).toHtml
    assert(html.contains("""x="10""""))
    assert(html.contains("""y="20""""))
    assert(html.contains("""width="100""""))
    assert(html.contains("""height="50""""))

  test("line attributes render correctly"):
    val html = (line | "0".x1 | "0".y1 | "100".x2 | "100".y2).toHtml
    assert(html.contains("""x1="0""""))
    assert(html.contains("""y1="0""""))
    assert(html.contains("""x2="100""""))
    assert(html.contains("""y2="100""""))

  test("polyline points attribute"):
    val html = (polyline | "0,0 50,50 100,0".points).toHtml
    assert(html.contains("""points="0,0 50,50 100,0""""))

  test("path d attribute"):
    val html = (path | "M10 10 L90 90".d).toHtml
    assert(html.contains("""d="M10 10 L90 90""""))

  // ============================================================
  // Presentation Attributes (TICKET-SVG-006)
  // ============================================================

  test("presentation attributes render correctly"):
    val html = (circle | "blue".fill | "black".stroke | "2".strokeWidth).toHtml
    assert(html.contains("""fill="blue""""))
    assert(html.contains("""stroke="black""""))
    assert(html.contains("""stroke-width="2""""))

  test("opacity attributes render correctly"):
    val html =
      (rect | "0.5".opacity | "0.8".fillOpacity | "0.7".strokeOpacity).toHtml
    assert(html.contains("""opacity="0.5""""))
    assert(html.contains("""fill-opacity="0.8""""))
    assert(html.contains("""stroke-opacity="0.7""""))

  test("transform attribute"):
    val html = (g | "rotate(45)".svgTransform).toHtml
    assert(html.contains("""transform="rotate(45)""""))

  test("stroke-linecap attribute"):
    val html = (line | "round".strokeLinecap).toHtml
    assert(html.contains("""stroke-linecap="round""""))

  test("stroke-dasharray attribute"):
    val html = (line | "5,5".strokeDasharray).toHtml
    assert(html.contains("""stroke-dasharray="5,5""""))

  // ============================================================
  // ViewBox and Namespace (TICKET-SVG-007)
  // ============================================================

  test("SvgNs.xmlns creates correct attribute"):
    assertEquals(
      Attr.render(SvgNs.xmlns),
      """xmlns="http://www.w3.org/2000/svg""""
    )

  test("SvgNs.viewBox helper"):
    val attr = SvgNs.viewBox(0, 0, 100, 100)
    assertEquals(Attr.render(attr), """viewBox="0 0 100 100"""")

  test("viewBox string extension"):
    val attr = "0 0 24 24".viewBox
    assertEquals(Attr.render(attr), """viewBox="0 0 24 24"""")

  test("preserveAspectRatio attribute"):
    val attr = "xMidYMid meet".preserveAspectRatio
    assertEquals(Attr.render(attr), """preserveAspectRatio="xMidYMid meet"""")

  // ============================================================
  // Gradient Attributes (TICKET-SVG-008)
  // ============================================================

  test("gradient stop attributes"):
    val html = (stop | "0%".offset | "red".stopColor).toHtml
    assert(html.contains("""offset="0%""""))
    assert(html.contains("""stop-color="red""""))

  test("gradient units attribute"):
    val html = (linearGradient | "userSpaceOnUse".gradientUnits).toHtml
    assert(html.contains("""gradientUnits="userSpaceOnUse""""))

  test("url reference for fill"):
    val attr = "#myGradient".fillRef
    assertEquals(Attr.render(attr), """fill="url(#myGradient)"""")

  test("url reference for clipPath"):
    val attr = "#myClip".clipPathRef
    assertEquals(Attr.render(attr), """clip-path="url(#myClip)"""")

  test("url reference for filter"):
    val attr = "#blur".filterRef
    assertEquals(Attr.render(attr), """filter="url(#blur)"""")

  // ============================================================
  // Complete Integration (TICKET-SVG-010)
  // ============================================================

  test("complete SVG icon renders correctly"):
    val html = (~(svg | SvgNs.xmlns | SvgNs.viewBox(0, 0, 24, 24))
      >>^ (circle | "12".cx | "12".cy | "10".r | "blue".fill)
      >>^ (path | "M12 2 L22 22 L2 22 Z".d | "red".fill)).toHtml

    assert(html.contains("""<svg xmlns="http://www.w3.org/2000/svg""""))
    assert(html.contains("""viewBox="0 0 24 24""""))
    assert(html.contains("<circle"))
    assert(html.contains("<path"))

  test("gradient definition"):
    val html = (~defs
      >>^ (~(linearGradient | "myGrad".id)
        >>^ (stop | "0%".offset | "red".stopColor)
        >>^ (stop | "100%".offset | "blue".stopColor))).toHtml

    assert(html.contains("<defs>"))
    assert(html.contains("<linearGradient"))
    assert(html.contains("<stop"))

  test("nested groups"):
    val html = (~svg
      >>^ (~(g | "rotate(45)".svgTransform)
        >>^ (rect | "10".x | "10".y | "80".svgWidth | "80".svgHeight))).toHtml

    assert(html.contains("<g"))
    assert(html.contains("""transform="rotate(45)""""))
    assert(html.contains("<rect"))

  test("text with styling"):
    val html =
      (~(text | "50".x | "50".y | "middle".textAnchor | "24px".fontSize)
        >>^ tags.Node.Text("Hello SVG")).toHtml

    assert(html.contains("<text"))
    assert(html.contains("""text-anchor="middle""""))
    assert(html.contains("""font-size="24px""""))
    assert(html.contains("Hello SVG"))

  test("pattern usage"):
    val html = (~svg
      >>^ (~defs
        >>^ (~(pattern | "dots".id | "10".svgWidth | "10".svgHeight | "userSpaceOnUse".patternUnits)
          >>^ (circle | "2".cx | "2".cy | "1".r | "gray".fill)))
      >>^ (rect | "0".x | "0".y | "100".svgWidth | "100".svgHeight | "#dots".fillRef)).toHtml

    assert(html.contains("<pattern"))
    assert(html.contains("""patternUnits="userSpaceOnUse""""))
    assert(html.contains("""fill="url(#dots)""""))

  test("filter effect"):
    val html = (~defs
      >>^ (~(filter | "blur".id)
        >>^ (feGaussianBlur | "SourceGraphic".in | "5".stdDeviation))).toHtml

    assert(html.contains("<filter"))
    assert(html.contains("<feGaussianBlur"))
    assert(html.contains("""stdDeviation="5""""))

  test("marker definition"):
    val html = (~defs
      >>^ (~(marker | "arrow".id | "10".markerWidth | "10".markerHeight | "0".refX | "5".refY | "auto".orient)
        >>^ (path | "M0 0 L10 5 L0 10 Z".d | "black".fill))).toHtml

    assert(html.contains("<marker"))
    assert(html.contains("""markerWidth="10""""))
    assert(html.contains("""orient="auto""""))
