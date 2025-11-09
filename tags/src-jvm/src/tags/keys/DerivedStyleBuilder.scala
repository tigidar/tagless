package tags.keys

import java.util.regex.Pattern

trait DerivedStyleBuilder[SS, DSP[_]] {

  protected def styleSetter(value: String): SS

  protected def derivedStyle[A](encode: (A => String)): DSP[A]

  protected def encodeUrlValue(url: String): String = {
    val escaped = replaceWithReplacer(
      url,
      DerivedStyleBuilder.urlPattern,
      DerivedStyleBuilder.urlReplacer
    )
    s""""$escaped"""" // #Note output is wrapped in double quotes
  }

  protected def encodeCalcValue(exp: String): String = {
    val escaped = replaceWithReplacer(
      exp,
      DerivedStyleBuilder.calcPattern,
      DerivedStyleBuilder.calcReplacer
    )
    s"$escaped" // #Note output is NOT wrapped in double quotes
  }

  // Helper method for regex-based replacements with a dynamic replacer
  private def replaceWithReplacer(input: String, pattern: Pattern, replacer: String => String): String = {
    val matcher = pattern.matcher(input)
    val result = new StringBuffer
    while (matcher.find()) {
      val replacement = replacer(matcher.group())
      val m = matcher.appendReplacement(result, replacement)
    }
    matcher.appendTail(result)
    result.toString
  }
}

object DerivedStyleBuilder {

  import java.util.regex.Pattern

  private val calcPattern: Pattern = Pattern.compile("[\"'\n\r\f\\\\;]")
  private val urlPattern: Pattern = Pattern.compile("[\"\n\r\f\\\\]")

  private val calcReplacer: String => String = _ => " "

  private val urlReplacer: String => String = {
    case "\"" => "%22"
    case "\\" => "%5C"
    case _    => " "
  }
}



/*
 *
 * This is a ScalaJS version, need to revisit this later.
import com.raquo.ew._

import scala.scalajs.js

trait DerivedStyleBuilder[SS, DSP[_]] {

  protected def styleSetter(value: String): SS

  // #Note: You can make this public if you wish
  protected def derivedStyle[A](encode: (A => String)): DSP[A]

  protected def encodeUrlValue(url: String): String = {
    // #TODO[Security] Review this.
    val escaped = url.ew.replace(
      DerivedStyleBuilder.urlPattern,
      DerivedStyleBuilder.urlReplacer
    ).str
    s""""$escaped"""" // #Note output is wrapped in double quotes
  }

  protected def encodeCalcValue(exp: String): String = {
    // #TODO[Security] Review this.
    val escaped = exp.ew.replace(
      DerivedStyleBuilder.calcPattern,
      DerivedStyleBuilder.calcReplacer
    ).str
    s"$escaped" // #Note output is NOT wrapped in double quotes
  }
}

object DerivedStyleBuilder {

  private val calcPattern = new js.RegExp("[\"\'\n\r\f\\\\;]", flags = "g")

  private val urlPattern = new js.RegExp("[\"\n\r\f\\\\]", flags = "g")

  private val calcReplacer: js.Function1[String, String] = { _ => " " }

  private val urlReplacer: js.Function1[String, String] = {
    case "\"" => "%22"
    case "\\" => "%5C"
    case _ => " "
  }
}
*/

