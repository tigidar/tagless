package tags

/** String extensions for creating attributes with shorthand syntax.
  *
  * Usage: div | "app".id | "container".cls a | "/about".href label |
  * "email".forId form | "/submit".action | "post".method "navigation".role
  * "label".ariaLabel
  */
object attrs:

  extension (s: String)
    // --- Original string attributes ---

    /** Create an id attribute */
    def id: Attr = Attr.Id(s)

    /** Create a class attribute */
    def cls: Attr = Attr.Class(s)

    /** Create an href attribute */
    def href: Attr = Attr.Href(s)

    /** Create a src attribute */
    def src: Attr = Attr.Src(s)

    /** Create an alt attribute */
    def alt: Attr = Attr.Alt(s)

    /** Create a title attribute */
    def title: Attr = Attr.Title(s)

    /** Create a name attribute */
    def name: Attr = Attr.Name(s)

    /** Create a value attribute */
    def value: Attr = Attr.Value(s)

    /** Create a type attribute */
    def tpe: Attr = Attr.Type(s)

    /** Create a placeholder attribute */
    def placeholder: Attr = Attr.Placeholder(s)

    // --- New string attributes (T004) ---

    /** Create a `for` attribute (on `<label>`). Named `forId` to avoid Scala
      * keyword conflict.
      */
    def forId: Attr = Attr.For(s)

    /** Create an `action` attribute (on `<form>`) */
    def action: Attr = Attr.Action(s)

    /** Create a `method` attribute (on `<form>`) */
    def method: Attr = Attr.Method(s)

    /** Create an `enctype` attribute (on `<form>`) */
    def enctype: Attr = Attr.Enctype(s)

    /** Create a `target` attribute (on `<a>`, `<form>`) */
    def target: Attr = Attr.Target(s)

    /** Create a `rel` attribute (on `<a>`, `<link>`) */
    def rel: Attr = Attr.Rel(s)

    /** Create a `role` attribute (ARIA role) */
    def role: Attr = Attr.Role(s)

    /** Create a `width` attribute */
    def width: Attr = Attr.Width(s)

    /** Create a `height` attribute */
    def height: Attr = Attr.Height(s)

    /** Create a `lang` attribute */
    def lang: Attr = Attr.Lang(s)

    /** Create a `dir` attribute */
    def dir: Attr = Attr.Dir(s)

    /** Create a `tabindex` attribute */
    def tabindex: Attr = Attr.Tabindex(s)

    /** Create a `colspan` attribute */
    def colspan: Attr = Attr.Colspan(s)

    /** Create a `rowspan` attribute */
    def rowspan: Attr = Attr.Rowspan(s)

    /** Create a `loading` attribute (`"lazy"` / `"eager"`) */
    def loading: Attr = Attr.Loading(s)

    // --- ARIA attributes (T005) ---

    /** Create an `aria-label` attribute */
    def ariaLabel: Attr = Attr.Aria("label", s)

    /** Create an `aria-hidden` attribute (takes `"true"` / `"false"`) */
    def ariaHidden: Attr = Attr.Aria("hidden", s)

    /** Create an `aria-describedby` attribute */
    def ariaDescribedby: Attr = Attr.Aria("describedby", s)

    /** Create an `aria-labelledby` attribute */
    def ariaLabelledby: Attr = Attr.Aria("labelledby", s)

    /** Create an `aria-expanded` attribute */
    def ariaExpanded: Attr = Attr.Aria("expanded", s)

    /** Create an `aria-controls` attribute */
    def ariaControls: Attr = Attr.Aria("controls", s)

    /** Create an `aria-live` attribute (takes `"polite"` / `"assertive"` /
      * `"off"`)
      */
    def ariaLive: Attr = Attr.Aria("live", s)

    /** Create an `aria-current` attribute */
    def ariaCurrent: Attr = Attr.Aria("current", s)

    /** Create a generic `aria-{name}` attribute (escape hatch for less common
      * ARIA attributes)
      */
    def aria(name: String): Attr = Attr.Aria(name, s)

    // --- Structured attributes ---

    /** Create a data-* attribute */
    def data(name: String): Attr = Attr.Data(name, s)

    /** Create a custom attribute */
    def attr(name: String): Attr = Attr.Custom(name, s)
