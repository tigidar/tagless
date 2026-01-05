package dom

final case class Tree[+A](value: A, children: Vector[Tree[A]]):

  def toHtml(using ev: A <:< Dom): String =
    val childrenHtml = children.map(_.toHtml).mkString

    value match
      case dom.Node.Element(tag, attrs) =>
        val attrsStr: (String, Option[String]) =
          if attrs.isEmpty then ("", None)
          else
            attrs.foldLeft((" ", Option.empty[String])) { (acc, attr) =>
              attr match
                case AttrNode.Text(t) =>
                  (acc._1, Option.apply(t))
                case other =>
                  (acc._1 + other.render() + " ", acc._2)
            }
        s"<$tag${attrsStr._1}>${attrsStr._2.getOrElse("")}$childrenHtml</$tag>\n"

      case dom.Node.VoidElement(tag, attrs) =>
        val attrsStr =
          if attrs.isEmpty then ""
          else " " + attrs.map(_.render()).mkString(" ")
        s"<$tag$attrsStr />"

      case dom.Node.Text(text) =>
        text // Text nodes do not have children

object Tree:
  def leaf[A](a: A): Tree[A] = Tree(a, Vector.empty)
