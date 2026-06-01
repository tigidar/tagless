package tags.crud

import tags.*
import tags.dsl.*
import tags.html.{div, button}
import tags.attrs.tpe
import tags.i18n.{I18n, Lang}
import _root_.html.lib.form.{FormDerivation, Form, FormConfig, FormEntry}
import _root_.html.lib.form.FormInterpreter

/** Abstraction for rendering both view and edit modes of a CRUD resource.
  *
  * Provides a unified interface for generating the HTML structure needed for
  * view/edit toggling with CrudHandler.
  *
  * ==ID Conventions==
  *
  * | Element        | ID Pattern            | Example           |
  * |:---------------|:----------------------|:------------------|
  * | View container | `{prefix}-view`       | `todo-view`       |
  * | Edit container | `{prefix}-edit`       | `todo-edit`       |
  * | Edit button    | `btn-{prefix}-edit`   | `btn-todo-edit`   |
  * | Cancel button  | `btn-{prefix}-cancel` | `btn-todo-cancel` |
  * | Save button    | `btn-{prefix}-save`   | `btn-todo-save`   |
  * | Delete button  | `btn-{prefix}-delete` | `btn-todo-delete` |
  * | Form           | `{prefix}-form`       | `todo-form`       |
  *
  * ==Usage==
  *
  * {{{
  * import tags.crud.*
  * import tags.dsl.*
  * import html.lib.form.*
  *
  * case class Todo(title: String, description: String)
  *
  * // Define labels and derivation
  * given FormDerivation[Todo] = FormDerivation.withLabels[Todo](...)
  *
  * // Create a CrudView
  * object TodoCrudView extends CrudView[Todo]:
  *   def viewContent(item: Todo)(using Lang): Node =
  *     Node.Element("div", Nil, Vector(
  *       Node.Text(s"Title: ${item.title}")
  *     ))
  *
  *   def submitLabel: I18n[?] = Labels.save
  *   def editLabel: I18n[?] = Labels.edit
  *   def cancelLabel: I18n[?] = Labels.cancel
  *
  * // Render both modes
  * val todo = Todo("Buy milk", "Get 2% milk")
  * val html = TodoCrudView.render(todo, "todo")
  * }}}
  */
trait CrudView[A](using fd: FormDerivation[A]):

  /** Render the view-mode content for an item.
    *
    * This is typically a table, card, or custom display of the item data.
    */
  def viewContent(item: A)(using Lang): Node

  /** Label for the form submit button. */
  def submitLabel: I18n[?]

  /** Label for the edit button. */
  def editLabel: I18n[?]

  /** Label for the cancel button. */
  def cancelLabel: I18n[?]

  /** Optional: Label for delete button. Override to enable delete button. */
  def deleteLabel: Option[I18n[?]] = None

  /** Render both view and edit sections.
    *
    * The edit section is initially hidden (has "hidden" class).
    *
    * @param item
    *   The item to display/edit
    * @param prefix
    *   The resource prefix for ID generation
    */
  def render(item: A, prefix: String)(using Lang): Cursor[D0, Normal] =
    // View section with edit button
    val viewSection = ~(div | Attr.Id(s"$prefix-view"))
      >>^ viewContent(item)
      >>^ (~(button | Attr.Id(s"btn-$prefix-edit") | "button".tpe)
        >>^ Node.text(editLabel.apply))

    // Edit section (hidden by default) with form and cancel button
    val form = fd.toForm(prefix, submitLabel)
    val formNode = FormInterpreter.toNode(form)

    val cancelButton = ~(button | Attr.Id(s"btn-$prefix-cancel") | "button".tpe)
      >>^ Node.text(cancelLabel.apply)

    val editChildren = Vector.newBuilder[Node]
    editChildren += formNode
    editChildren += cancelButton.focus

    deleteLabel.foreach { label =>
      val deleteButton =
        ~(button | Attr.Id(s"btn-$prefix-delete") | "button".tpe)
          >>^ Node.text(label.apply)
      editChildren += deleteButton.focus
    }

    val editSection = Node.Element(
      "div",
      List(Attr.Id(s"$prefix-edit"), Attr.Class("hidden")),
      editChildren.result()
    )

    ~div >>^ viewSection >>^ editSection

object CrudView:

  /** Create a simple CrudView from functions.
    *
    * @param viewFn
    *   Function to render view content
    * @param submit
    *   Submit button label
    * @param edit
    *   Edit button label
    * @param cancel
    *   Cancel button label
    * @param delete
    *   Optional delete button label
    */
  def apply[A](
      viewFn: (A, Lang) => Node,
      submit: I18n[?],
      edit: I18n[?],
      cancel: I18n[?],
      delete: Option[I18n[?]] = None
  )(using fd: FormDerivation[A]): CrudView[A] =
    new CrudView[A]:
      def viewContent(item: A)(using lang: Lang): Node = viewFn(item, lang)
      def submitLabel: I18n[?] = submit
      def editLabel: I18n[?] = edit
      def cancelLabel: I18n[?] = cancel
      override def deleteLabel: Option[I18n[?]] = delete
