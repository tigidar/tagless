package tags

import munit.FunSuite
import tags.dsl.*
import tags.html.*
import tags.attrs.*
import tags.viz.*
import tags.viz.dsl.*
import tags.TestI18n.{given, *}

class VizSpec extends FunSuite:

  // ============================================================
  // Basic visualization
  // ============================================================

  test("simple div visualizes correctly"):
    val viz = (~div).visualize
    assertEquals(viz.root.tag, "div")
    assertEquals(viz.root.nodeType, NodeType.Element)
    assertEquals(viz.stats.totalNodes, 1)

  test("nested elements build correct tree"):
    val viz = (~div >> p >> span).^^.visualize
    assertEquals(viz.root.tag, "div")
    assertEquals(viz.root.children.size, 1)
    assertEquals(viz.root.children.head.tag, "p")
    assertEquals(viz.root.children.head.children.head.tag, "span")
    assertEquals(viz.stats.maxDepth, 2)

  test("void element visualization"):
    val viz = (~img).visualize
    assertEquals(viz.root.tag, "img")
    assertEquals(viz.root.nodeType, NodeType.Void)
    assertEquals(viz.stats.voidCount, 1)

  test("text node visualization"):
    val viz = (~div >> "Hello".i18n).^.visualize
    assertEquals(viz.root.children.size, 1)
    assertEquals(viz.root.children.head.nodeType, NodeType.Text)
    assertEquals(viz.root.children.head.textContent, Some("Hello"))
    assertEquals(viz.stats.textCount, 1)

  // ============================================================
  // Attributes and IDs
  // ============================================================

  test("attributes captured in visualization"):
    val viz = (~(div | "app".id | "container".cls)).visualize
    assertEquals(viz.root.attributes.get("id"), Some("app"))
    assertEquals(viz.root.attributes.get("class"), Some("container"))
    assertEquals(viz.stats.idsFound, Set("app"))

  test("label with id"):
    val viz = (~(div | "my-app".id)).visualize
    assertEquals(viz.root.shortLabel, "div#my-app")

  test("label with id and class"):
    val viz = (~(div | "my-app".id | "container main".cls)).visualize
    assertEquals(viz.root.label, "div#my-app.container.main")

  // ============================================================
  // Component marking
  // ============================================================

  test("asComponent adds data attributes"):
    val tree = (~div >> p >> span).^^.asComponent("MyWidget", "ui")
    val html = tree.toHtml
    assert(
      html.contains("""data-component="MyWidget""""),
      s"Missing data-component in: $html"
    )
    assert(
      html.contains("""data-component-category="ui""""),
      s"Missing category in: $html"
    )

  test("component detected in visualization"):
    val viz = (~div >> p).^.asComponent("Navigation", "layout").visualize
    assertEquals(viz.root.nodeType, NodeType.Component)
    assertEquals(viz.root.component.map(_.name), Some("Navigation"))
    assertEquals(viz.root.component.map(_.category), Some("layout"))
    assertEquals(viz.stats.componentCount, 1)

  test("asComponent with default category"):
    val viz = (~nav >> ul).^.asComponent("MainNav").visualize
    assertEquals(viz.root.component.map(_.category), Some("default"))

  // ============================================================
  // Statistics
  // ============================================================

  test("stats count elements correctly"):
    val viz = (~div >> ul >>^ (li * 3)).visualize
    assertEquals(viz.stats.totalNodes, 5) // div + ul + 3 li
    assertEquals(viz.stats.elementCount, 5)
    assertEquals(viz.stats.elementsByTag.get("li"), Some(3))

  test("mixed content stats"):
    val viz =
      (~div >>^ p("Hello".i18n) >>^ img >>^ span("World".i18n)).visualize
    assertEquals(viz.stats.elementCount, 3) // div, p, span
    assertEquals(viz.stats.voidCount, 1) // img
    assertEquals(viz.stats.textCount, 2) // "Hello", "World"

  // ============================================================
  // ASCII tree rendering
  // ============================================================

  test("ascii tree renders simple structure"):
    val tree = (~div >> p >> span).^^.toAsciiTree
    assert(tree.contains("div"), s"Missing div in:\n$tree")
    assert(tree.contains("p"), s"Missing p in:\n$tree")
    assert(tree.contains("span"), s"Missing span in:\n$tree")
    assert(
      tree.contains("└──") || tree.contains("├──"),
      s"Missing tree connectors in:\n$tree"
    )

  test("ascii tree shows text content"):
    val tree = (~div >> "Hello World".i18n).^.toAsciiTree
    assert(tree.contains("#text"), s"Missing #text in:\n$tree")
    assert(tree.contains("Hello"), s"Missing text content in:\n$tree")

  // ============================================================
  // D3 JSON serialization
  // ============================================================

  test("d3 json has required fields"):
    val json = (~div >> p).^.toD3Json
    assert(json.contains(""""name":"""), s"Missing name field in:\n$json")
    assert(json.contains(""""tagName":"""), s"Missing tagName field in:\n$json")
    assert(
      json.contains(""""children":"""),
      s"Missing children field in:\n$json"
    )
    assert(json.contains(""""depth":"""), s"Missing depth field in:\n$json")

  test("d3 json includes component info"):
    val json = (~div >> p).^.asComponent("Widget").toD3Json
    assert(
      json.contains(""""component":"""),
      s"Missing component field in:\n$json"
    )
    assert(
      json.contains(""""name": "Widget""""),
      s"Missing component name in:\n$json"
    )

  test("d3 json includes attributes"):
    val json = (~(div | "app".id | "main".cls)).toD3Json
    assert(json.contains(""""attributes":"""), s"Missing attributes in:\n$json")
    assert(json.contains(""""id": "app""""), s"Missing id attribute in:\n$json")
    assert(
      json.contains(""""class": "main""""),
      s"Missing class attribute in:\n$json"
    )

  // ============================================================
  // Mermaid rendering
  // ============================================================

  test("mermaid diagram renders"):
    val mermaid = (~div >> p >> span).^^.toMermaid
    assert(
      mermaid.startsWith("flowchart TD"),
      s"Missing flowchart header in:\n$mermaid"
    )
    assert(mermaid.contains("-->"), s"Missing edges in:\n$mermaid")

  // ============================================================
  // Node extension methods
  // ============================================================

  test("node visualize extension"):
    val node = Node.element("div")
    val viz = node.visualize
    assertEquals(viz.root.tag, "div")

  test("node toAsciiTree extension"):
    val node = Node.Element("nav", Nil, Vector(Node.element("ul")))
    val tree = node.toAsciiTree
    assert(tree.contains("nav"), s"Missing nav in:\n$tree")
    assert(tree.contains("ul"), s"Missing ul in:\n$tree")
