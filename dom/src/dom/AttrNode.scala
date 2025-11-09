package dom

enum AttrNode:
  self =>
  case HtmlAttr(name: String, value: String)
  case Setter(name: String)
  case Prop(name: String, value: String)
  case Text(value: String)
  case Tailwind(name: String, value: String)
  case Aria(name: String, value: String)

  def render(): String = self match
    case HtmlAttr(name, value) => s"""$name="$value""""
    case Prop(name, value)     => s"""$name="$value""""
    case Text(value)           => value
    case Setter(value)         => value
    case Tailwind(name, value) => s"""${name}="$value""""
    case Aria(name, value)     => s"""${name}="$value""""

