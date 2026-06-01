package tags

/** Attribute representation for HTML elements.
  *
  * Using an ADT allows type-safe attribute handling and proper rendering for
  * different attribute types (boolean attrs, data attrs, etc.)
  */
enum Attr derives CanEqual:
  // --- String attributes ---
  case Id(value: String)
  case Class(value: String)
  case Href(value: String)
  case Src(value: String)
  case Alt(value: String)
  case Title(value: String)
  case Name(value: String)
  case Value(value: String)
  case Type(value: String)
  case Placeholder(value: String)
  case For(value: String)
  case Action(value: String)
  case Method(value: String)
  case Enctype(value: String)
  case Target(value: String)
  case Rel(value: String)
  case Role(value: String)
  case Width(value: String)
  case Height(value: String)
  case Lang(value: String)
  case Dir(value: String)
  case Tabindex(value: String)
  case Colspan(value: String)
  case Rowspan(value: String)
  case Loading(value: String)
  // --- Boolean attributes ---
  case Disabled
  case Checked
  case Required
  case Readonly
  case Autoplay
  case Controls
  case Loop
  case Muted
  case Open
  case Hidden
  case Autofocus
  case Multiple
  case Selected
  case Contenteditable
  case Draggable
  case Novalidate
  // --- Structured attributes ---
  case Aria(name: String, value: String)
  case Data(name: String, value: String)
  case Style(property: String, value: String)
  case Custom(name: String, value: String)

object Attr:
  /** Render an attribute to HTML string */
  def render(attr: Attr): String = attr match
    case Id(v)           => s"""id="$v""""
    case Class(v)        => s"""class="$v""""
    case Href(v)         => s"""href="$v""""
    case Src(v)          => s"""src="$v""""
    case Alt(v)          => s"""alt="$v""""
    case Title(v)        => s"""title="$v""""
    case Name(v)         => s"""name="$v""""
    case Value(v)        => s"""value="$v""""
    case Type(v)         => s"""type="$v""""
    case Placeholder(v)  => s"""placeholder="$v""""
    case For(v)          => s"""for="$v""""
    case Action(v)       => s"""action="$v""""
    case Method(v)       => s"""method="$v""""
    case Enctype(v)      => s"""enctype="$v""""
    case Target(v)       => s"""target="$v""""
    case Rel(v)          => s"""rel="$v""""
    case Role(v)         => s"""role="$v""""
    case Width(v)        => s"""width="$v""""
    case Height(v)       => s"""height="$v""""
    case Lang(v)         => s"""lang="$v""""
    case Dir(v)          => s"""dir="$v""""
    case Tabindex(v)     => s"""tabindex="$v""""
    case Colspan(v)      => s"""colspan="$v""""
    case Rowspan(v)      => s"""rowspan="$v""""
    case Loading(v)      => s"""loading="$v""""
    case Disabled        => "disabled"
    case Checked         => "checked"
    case Required        => "required"
    case Readonly        => "readonly"
    case Autoplay        => "autoplay"
    case Controls        => "controls"
    case Loop            => "loop"
    case Muted           => "muted"
    case Open            => "open"
    case Hidden          => "hidden"
    case Autofocus       => "autofocus"
    case Multiple        => "multiple"
    case Selected        => "selected"
    case Contenteditable => "contenteditable"
    case Draggable       => "draggable"
    case Novalidate      => "novalidate"
    case Aria(name, v)   => s"""aria-$name="$v""""
    case Data(name, v)   => s"""data-$name="$v""""
    case Style(prop, v)  => s"""style="$prop: $v""""
    case Custom(name, v) => s"""$name="$v""""

  /** Render a list of attributes to HTML string */
  def renderAll(attrs: List[Attr]): String =
    if attrs.isEmpty then ""
    else " " + attrs.map(render).mkString(" ")
