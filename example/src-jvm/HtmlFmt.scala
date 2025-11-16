package ex

import os.*

object HtmlFmt:
  private val webDir = os.pwd / "example" / "web"
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
        stdin = html, // pipe HTML via stdin
        check = false, // we'll handle non-zero exit ourselves
        timeout = timeoutMs
      )

    if res.exitCode == 0 then res.out.text()
    else
      val err = res.err.text()
      throw new RuntimeException(s"Prettier failed (${res.exitCode}):\n$err")

  /** Format then write to disk (overwrites). */
  def formatHtmlToFile(html: String, outPath: os.Path): Unit =
    val pretty = formatHtml(html)
    println(pretty)
    println(outPath)
    os.makeDir.all(outPath / os.up) // ensure parent dir
    os.write.over(outPath, pretty)
