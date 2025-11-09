package tags

import tags.gen.HtmlTags
import tags.defs.attrs.*
import tags.defs.complex.*
import tags.defs.props.*

trait Tags
    extends HtmlTags
    with HtmlAttrs
    with TailwindAttrs
    with Props
    with ComplexHtmlKeys

val Aria = new AriaAttrs {}

//with GlobalEventProps
//with StyleProps
//with ComplexHtmlKeys
val T = new Tags {}
