package md

/** Parses inline Markdown syntax into InlineMarkdown AST.
  *
  * Supports:
  *   - `**bold**` → Bold(Text("bold"))
  *   - `*italic*` → Italic(Text("italic"))
  *   - `__underline__` → Underline(Text("underline"))
  *   - `` `code` `` → Code("code")
  *   - `$$math$$` → MathBlock("math")
  *   - `$math$` → Math("math")
  *   - plain text → Text("plain text")
  *   - mixed content → Sequence(...)
  */
object InlineMarkdownParser:

  def parse(input: String): InlineMarkdown =
    val items = Vector.newBuilder[InlineMarkdown]
    val plain = new StringBuilder
    var i = 0
    val len = input.length

    def flushPlain(): Unit =
      if plain.nonEmpty then
        items += InlineMarkdown.Text(plain.toString)
        plain.clear()

    while i < len do
      val c = input.charAt(i)

      if c == '$' && i + 1 < len && input.charAt(i + 1) == '$' then
        // Display math: $$...$$
        val start = i + 2
        val end = input.indexOf("$$", start)
        if end < 0 then
          plain.append('$')
          plain.append('$')
          i += 2
        else
          flushPlain()
          items += InlineMarkdown.MathBlock(input.substring(start, end))
          i = end + 2
      else if c == '$' then
        // Inline math: $...$
        val start = i + 1
        val end = input.indexOf('$', start)
        if end < 0 then
          plain.append(c)
          i += 1
        else
          flushPlain()
          items += InlineMarkdown.Math(input.substring(start, end))
          i = end + 1
      else if c == '`' then
        // Backtick code span
        val start = i + 1
        val end = input.indexOf('`', start)
        if end < 0 then
          plain.append(c)
          i += 1
        else
          flushPlain()
          items += InlineMarkdown.Code(input.substring(start, end))
          i = end + 1
      else if c == '_' && i + 1 < len && input.charAt(i + 1) == '_' then
        // Underline: __...__
        val start = i + 2
        val end = input.indexOf("__", start)
        if end < 0 then
          plain.append('_')
          plain.append('_')
          i += 2
        else
          flushPlain()
          val inner = input.substring(start, end)
          items += InlineMarkdown.Underline(parse(inner))
          i = end + 2
      else if c == '*' && i + 1 < len && input.charAt(i + 1) == '*' then
        // Bold: **...**
        val start = i + 2
        val end = input.indexOf("**", start)
        if end < 0 then
          plain.append('*')
          plain.append('*')
          i += 2
        else
          flushPlain()
          val inner = input.substring(start, end)
          items += InlineMarkdown.Bold(parse(inner))
          i = end + 2
      else if c == '*' then
        // Italic: *...*
        val start = i + 1
        val end = input.indexOf('*', start)
        if end < 0 then
          plain.append(c)
          i += 1
        else
          flushPlain()
          val inner = input.substring(start, end)
          items += InlineMarkdown.Italic(parse(inner))
          i = end + 1
      else
        plain.append(c)
        i += 1
    end while

    flushPlain()

    items.result() match
      case Vector()    => InlineMarkdown.Text("")
      case Vector(one) => one
      case many        => InlineMarkdown.Sequence(many)
