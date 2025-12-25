package dom

enum Tree[+A]:
  self =>
  case Node(value: A, children: Vector[Tree[A]])

  def toHtml[B >: A](using ev: B =:= Dom): String = self match
    case Node(value, children) =>
      val childrenHtml = children.map(_.toHtml).mkString

      value match
        case dom.Node.Element(tag, attrs) =>
          val (attrsStr, textContent) =
            if attrs.isEmpty then ("", None)
            else
              attrs.foldLeft((" ", Option.empty[String])) { (acc, attr) =>
                attr match
                  case AttrNode.Text(t) => (acc._1, Some(t))
                  case other            => (acc._1 + other.render() + " ", acc._2)
              }
          s"<$tag${attrsStr}>${textContent.getOrElse("")}$childrenHtml</$tag>\n"

        case dom.Node.VoidElement(tag, attrs) =>
          val attrsStr =
            if attrs.isEmpty then ""
            else " " + attrs.map(_.render()).mkString(" ")
          s"<$tag$attrsStr />"

        case dom.Node.Text(text) =>
          text
