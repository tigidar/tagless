package html.lib

import munit.*
import tagless.dsl.{given, *}
import tags.T.*
import html.lib.table.*
import html.lib.table.TableConverter.*

class TableConverterTest extends FunSuite:

  // Use sourcecode.File() to get the actual source file location at compile time,
  // then navigate to project root (6 levels up from html/test/src/html4s/lib/TableConverterTest.scala)
  private val thisFile = os.Path(sourcecode.File())
  private val projectRoot = thisFile / os.up / os.up / os.up / os.up / os.up / os.up
  private val webDir = projectRoot / "example" / "web"
  private val prettierBin = webDir / "node_modules" / ".bin" / "prettier"

  /** Format an HTML string using the project's local Prettier. */
  def formatHtml(html: String, timeoutMs: Int = 20_000): String =
    val cmd =
      if os.exists(prettierBin) then
        Seq(prettierBin.toString, "--parser", "html")
      else
        // Fallback to npx if local bin isn't present
        Seq("npx", "prettier", "--parser", "html")

    val res = os
      .proc(cmd)
      .call(
        cwd = webDir,
        stdin = html,
        check = false,
        timeout = timeoutMs
      )

    if res.exitCode == 0 then res.out.text()
    else
      val err = res.err.text()
      throw new RuntimeException(s"Prettier failed (${res.exitCode}):\n$err")

  // === Simple Table Tests ===

  test("Simple table with header and body"):
    // format: off
    val t = Table.^
      |- "Name"   | "Age"  | "City"
      || "Alice"  | "30"   | "NYC"
      |+ "Bob"    | "25"   | "LA"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Age</th>
            <th>City</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Alice</td>
            <td>30</td>
            <td>NYC</td>
          </tr>
          <tr>
            <td>Bob</td>
            <td>25</td>
            <td>LA</td>
          </tr>
        </tbody>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Table with caption, header and body"):
    // format: off
    val t = Table.^
      -- "Employee Directory"
      |- "Name"   | "Department"
      || "John"   | "Engineering"
      |+ "Jane"   | "Marketing"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <caption>Employee Directory</caption>
        <thead>
          <tr>
            <th>Name</th>
            <th>Department</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>John</td>
            <td>Engineering</td>
          </tr>
          <tr>
            <td>Jane</td>
            <td>Marketing</td>
          </tr>
        </tbody>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Table with multiple header rows"):
    // format: off
    val t = Table.^
      |- "Category"  | "Q1"  | "Q2"
      |+ "SubCat"    | "Jan" | "Apr"
      || "Product A" | "100" | "150"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <thead>
          <tr>
            <th>Category</th>
            <th>Q1</th>
            <th>Q2</th>
          </tr>
          <tr>
            <th>SubCat</th>
            <th>Jan</th>
            <th>Apr</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Product A</td>
            <td>100</td>
            <td>150</td>
          </tr>
        </tbody>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Table with footer"):
    // format: off
    val t = Table.^
      |- "Item"   | "Price"
      || "Widget" | "$10"
      |+ "Gadget" | "$20"
      |^ "Total: $30"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <thead>
          <tr>
            <th>Item</th>
            <th>Price</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Widget</td>
            <td>$10</td>
          </tr>
          <tr>
            <td>Gadget</td>
            <td>$20</td>
          </tr>
        </tbody>
        <tfoot>
          <tr>
            <td>Total: $30</td>
          </tr>
        </tfoot>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Full table with caption, header, body and footer"):
    // format: off
    val t = Table.^
      -- "Sales Report Q1"
      |- "Product" | "Units" | "Revenue"
      || "Alpha"   | "100"   | "$1000"
      |+ "Beta"    | "200"   | "$3000"
      |+ "Gamma"   | "150"   | "$2250"
      |^ "Total Revenue: $6250"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <caption>Sales Report Q1</caption>
        <thead>
          <tr>
            <th>Product</th>
            <th>Units</th>
            <th>Revenue</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Alpha</td>
            <td>100</td>
            <td>$1000</td>
          </tr>
          <tr>
            <td>Beta</td>
            <td>200</td>
            <td>$3000</td>
          </tr>
          <tr>
            <td>Gamma</td>
            <td>150</td>
            <td>$2250</td>
          </tr>
        </tbody>
        <tfoot>
          <tr>
            <td>Total Revenue: $6250</td>
          </tr>
        </tfoot>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Table with single row"):
    // format: off
    val t = Table.^
      |- "Single" | "Row" | "Table"
      || "A"      | "B"   | "C"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <thead>
          <tr>
            <th>Single</th>
            <th>Row</th>
            <th>Table</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>A</td>
            <td>B</td>
            <td>C</td>
          </tr>
        </tbody>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  test("Table with many body rows"):
    // format: off
    val t = Table.^
      |- "ID" | "Value"
      || "1"  | "One"
      |+ "2"  | "Two"
      |+ "3"  | "Three"
      |+ "4"  | "Four"
      |+ "5"  | "Five"
    // format: on

    val html = t.toHtml
    val expected = """
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>1</td>
            <td>One</td>
          </tr>
          <tr>
            <td>2</td>
            <td>Two</td>
          </tr>
          <tr>
            <td>3</td>
            <td>Three</td>
          </tr>
          <tr>
            <td>4</td>
            <td>Four</td>
          </tr>
          <tr>
            <td>5</td>
            <td>Five</td>
          </tr>
        </tbody>
      </table>
    """
    assertEquals(formatHtml(html), formatHtml(expected))

  // === toDom Extension Test ===

  test("toDom returns cursor that can be used in DSL"):
    // format: off
    val t = Table.^
      |- "Col1" | "Col2"
      || "A"    | "B"
    // format: on

    val cursor = t.toDom
    val tree = cursor.seal
    val html = tree.toHtml

    assert(html.contains("<table"), "should contain table tag")
    assert(html.contains("<thead"), "should contain thead tag")
    assert(html.contains("<tbody"), "should contain tbody tag")
    assert(html.contains(">Col1<"), "should contain header cell content")
    assert(html.contains(">A<"), "should contain data cell content")
