package tags

/** Builder for composing CSS class names.
  *
  * Usage:
  * {{{
  * import tags.classes.*
  *
  * ClassList("card")
  *   .add("shadow")
  *   .addWhen(isActive, "active")
  *   .toAttr
  * }}}
  */
final case class ClassList(names: Vector[String]):

  /** Add a class name unconditionally */
  def add(name: String): ClassList =
    ClassList(names :+ name)

  /** Add multiple class names unconditionally */
  def add(moreNames: String*): ClassList =
    ClassList(names ++ moreNames)

  /** Add a class name if condition is true */
  def addWhen(condition: Boolean, name: String): ClassList =
    if condition then ClassList(names :+ name) else this

  /** Add a class name if condition is false */
  def addUnless(condition: Boolean, name: String): ClassList =
    if !condition then ClassList(names :+ name) else this

  /** Add multiple conditional classes */
  def addWhen(condition: Boolean, moreNames: String*): ClassList =
    if condition then ClassList(names ++ moreNames) else this

  /** Convert to Attr.Class, joining with spaces */
  def toAttr: Attr =
    Attr.Class(names.mkString(" "))

  /** Check if any classes are present */
  def nonEmpty: Boolean = names.nonEmpty

  /** Check if empty */
  def isEmpty: Boolean = names.isEmpty

object ClassList:
  /** Create a ClassList with initial class(es) */
  def apply(names: String*): ClassList =
    ClassList(names.toVector)

  /** Empty ClassList */
  val empty: ClassList = ClassList(Vector.empty)

/** Merge multiple Attr.Class into a single Attr.Class. Non-Class attributes are
  * preserved as-is.
  */
def mergeClasses(attrs: List[Attr]): List[Attr] =
  val (classAttrs, otherAttrs) = attrs.partition {
    case Attr.Class(_) => true
    case _             => false
  }
  val mergedClass = classAttrs.collect { case Attr.Class(v) => v }.mkString(" ")
  if mergedClass.isEmpty then otherAttrs
  else Attr.Class(mergedClass) :: otherAttrs

object classes:

  extension (name: String)
    /** Create class if condition is true, None otherwise */
    def clsWhen(condition: Boolean): Option[Attr] =
      if condition then Some(Attr.Class(name)) else None

    /** Create class if condition is false, None otherwise */
    def clsUnless(condition: Boolean): Option[Attr] =
      if !condition then Some(Attr.Class(name)) else None

    /** Wrap in Some for use with classesOpt */
    def when(condition: Boolean): Option[String] =
      if condition then Some(name) else None

    /** Wrap in Some for use with classesOpt (inverted) */
    def unless(condition: Boolean): Option[String] =
      if !condition then Some(name) else None

  extension [K <: ElementKind](tag: Tag[K])
    /** Add optional attribute (no-op if None) */
    def |?(optAttr: Option[Attr]): Tag[K] =
      optAttr match
        case Some(attr) => tag | attr
        case None       => tag

    /** Merge all Class attributes on this tag into one */
    def mergeClasses: Tag[K] =
      tag.copy(attrs = tags.mergeClasses(tag.attrs))

  /** Create an Attr.Class from multiple class names.
    *
    * Usage:
    * {{{
    * div | classes("card", "shadow", "rounded")
    * }}}
    */
  def apply(names: String*): Attr =
    Attr.Class(names.mkString(" "))

  /** Create an Attr.Class from a sequence of optional class names.
    *
    * Usage:
    * {{{
    * div | classesOpt(Some("card"), None, Some("rounded"))
    * }}}
    */
  def opt(names: Option[String]*): Attr =
    Attr.Class(names.flatten.mkString(" "))
