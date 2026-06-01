package tags.meta

import tags.{Node, Attr}

/** Helper object for creating <meta> tags with common patterns.
  *
  * All methods return Node.VoidElement instances.
  */
object Meta:

  /** Create a charset meta tag.
    *
    * @param encoding
    *   The character encoding (e.g., "utf-8")
    * @return
    *   <meta charset="..." />
    */
  def charset(encoding: String): Node.VoidElement =
    Node.VoidElement("meta", List(Attr.Custom("charset", encoding)))

  /** Create a viewport meta tag.
    *
    * @param content
    *   The viewport content (e.g., "width=device-width, initial-scale=1")
    * @return
    *   <meta name="viewport" content="..." />
    */
  def viewport(content: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name("viewport"),
        Attr.Custom("content", content)
      )
    )

  /** Create a description meta tag.
    *
    * @param content
    *   The page description
    * @return
    *   <meta name="description" content="..." />
    */
  def description(content: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name("description"),
        Attr.Custom("content", content)
      )
    )

  /** Create a keywords meta tag.
    *
    * @param keywords
    *   The keywords (will be joined with ", ")
    * @return
    *   <meta name="keywords" content="..." />
    */
  def keywords(keywords: String*): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name("keywords"),
        Attr.Custom("content", keywords.mkString(", "))
      )
    )

  /** Create an author meta tag.
    *
    * @param name
    *   The author name
    * @return
    *   <meta name="author" content="..." />
    */
  def author(name: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name("author"),
        Attr.Custom("content", name)
      )
    )

  /** Create a robots meta tag.
    *
    * @param directives
    *   The robot directives (will be joined with ", ")
    * @return
    *   <meta name="robots" content="..." />
    */
  def robots(directives: String*): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name("robots"),
        Attr.Custom("content", directives.mkString(", "))
      )
    )

  /** Create an Open Graph meta tag.
    *
    * @param property
    *   The OG property name (without "og:" prefix)
    * @param content
    *   The property content
    * @return
    *   <meta property="og:..." content="..." />
    */
  def og(property: String, content: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Custom("property", s"og:$property"),
        Attr.Custom("content", content)
      )
    )

  /** Create a Twitter Card meta tag.
    *
    * @param name
    *   The Twitter card property name (without "twitter:" prefix)
    * @param content
    *   The property content
    * @return
    *   <meta name="twitter:..." content="..." />
    */
  def twitter(name: String, content: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Name(s"twitter:$name"),
        Attr.Custom("content", content)
      )
    )

  /** Create an http-equiv meta tag.
    *
    * @param equiv
    *   The http-equiv value (e.g., "refresh")
    * @param content
    *   The content value
    * @return
    *   <meta http-equiv="..." content="..." />
    */
  def httpEquiv(equiv: String, content: String): Node.VoidElement =
    Node.VoidElement(
      "meta",
      List(
        Attr.Custom("http-equiv", equiv),
        Attr.Custom("content", content)
      )
    )

  /** Returns a vector of common default meta tags (charset utf-8 and responsive
    * viewport).
    *
    * @return
    *   Vector containing charset and viewport meta tags
    */
  def defaults: Vector[Node.VoidElement] =
    Vector(
      charset("utf-8"),
      viewport("width=device-width, initial-scale=1")
    )
