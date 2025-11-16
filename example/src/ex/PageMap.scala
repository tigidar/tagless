package ex

import html.lib.id
<<<<<<< HEAD

enum PageType[+A]:
  case ContentOnly(id: String)
  case State(id: String, a: A)

  def elementId: String = this match
    case ContentOnly(id) => id
    case State(id, _)    => id

enum PageState:
  case TodoList(
      items: List[String]
  )
  case ErrorMessage(
      message: String
  )
=======
import html.lib.{CssClass, isHidden}
import scala.compiletime.constValue

// Re-export CSS types for convenience (but not Theme to avoid conflicts with html.lib.Theme)
export html.lib.{CssClass, isHidden}

/** Type-safe page state variants */
enum PageState derives CanEqual:
  case TodoList(items: List[String])
  case ErrorMessage(message: String)
  case NoState

/** A page definition that links an element ID to its optional state.
  * Uses singleton string types for compile-time ID verification.
  */
sealed trait PageDef[+S <: PageState]:
  type ElementId <: String & Singleton
  def elementId: ElementId
  def initialState: S

object PageDef:
  /** A page with no associated state (content-only) */
  final class ContentOnly[Id <: String & Singleton](
      val elementId: Id
  ) extends PageDef[PageState.NoState.type]:
    type ElementId = Id
    val initialState: PageState.NoState.type = PageState.NoState

  /** A page with associated state */
  final class Stateful[Id <: String & Singleton, S <: PageState](
      val elementId: Id,
      val initialState: S
  ) extends PageDef[S]:
    type ElementId = Id

/** Type-safe route entry linking a button ID to a page definition */
final class Route[ButtonId <: String & Singleton, P <: PageDef[?]](
    val buttonId: ButtonId,
    val page: P
)

object Route:
  def apply[B <: String & Singleton, P <: PageDef[?]](
      buttonId: B,
      page: P
  ): Route[B, P] = new Route(buttonId, page)
>>>>>>> 60956bd (cleanup scalajs module, improve typesafety)

object PageMap:

  object NavButtons:
    val homeId: "home-button" = id["home-button"]
    val aboutId: "about-button" = id["about-button"]
    val todoId: "todo-button" = id["todo-button"]
    val contactId: "contact-button" = id["contact-button"]

  object Pages:
    val welcomeId: "welcome-content" = id["welcome-content"]
    val aboutId: "about-content" = id["about-content"]
    val todoListId: "todo-list" = id["todo-list"]
    val contactId: "contact-content" = id["contact-content"]
    val errorId: "error-page" = id["error-page"]

<<<<<<< HEAD
  val router: Map[String, PageType[PageState]] = Map(
    NavButtons.homeId -> PageType.ContentOnly(Pages.welcomeId),
    NavButtons.aboutId -> PageType.ContentOnly(Pages.aboutId),
    NavButtons.todoId -> PageType
      .State(Pages.todoListId, PageState.TodoList(Nil)),
    NavButtons.contactId -> PageType.ContentOnly(Pages.contactId),
    Pages.errorId -> PageType.State(
      Pages.errorId,
      PageState.ErrorMessage(
        "Something went wrong, sorry about the inconvenience."
      )
    )
=======
  // Type-safe page definitions
  object PageDefs:
    val welcome = PageDef.ContentOnly(Pages.welcomeId)
    val about = PageDef.ContentOnly(Pages.aboutId)
    val todoList = PageDef.Stateful(
      Pages.todoListId,
      PageState.TodoList(Nil)
    )
    val contact = PageDef.ContentOnly(Pages.contactId)
    val error: PageDef.Stateful["error-page", PageState.ErrorMessage] = PageDef.Stateful(
      Pages.errorId,
      PageState.ErrorMessage("Something went wrong, sorry about the inconvenience.")
    )

  // Type-safe routes
  object Routes:
    val home = Route(NavButtons.homeId, PageDefs.welcome)
    val about = Route(NavButtons.aboutId, PageDefs.about)
    val todo = Route(NavButtons.todoId, PageDefs.todoList)
    val contact = Route(NavButtons.contactId, PageDefs.contact)

  // All routes for runtime lookup (still needed for dynamic dispatch)
  val allRoutes: List[Route[?, ?]] = List(
    Routes.home,
    Routes.about,
    Routes.todo,
    Routes.contact
  )

  // Runtime lookup map - uses String keys but values are type-safe PageDefs
  val routeByButtonId: Map[String, PageDef[?]] =
    allRoutes.map(r => r.buttonId -> r.page).toMap

  /** Find a page definition by button ID */
  def findPage(buttonId: String): Option[PageDef[?]] =
    routeByButtonId.get(buttonId)

  /** The initial/default page to show */
  val initialPage: PageDef[PageState.NoState.type] = PageDefs.welcome

  /** The error page to show on failures */
  val errorPage: PageDef.Stateful["error-page", PageState.ErrorMessage] = PageDefs.error

  /** All page element IDs for validation */
  val allPageIds: Set[String] = Set(
    Pages.welcomeId,
    Pages.aboutId,
    Pages.todoListId,
    Pages.contactId,
    Pages.errorId
>>>>>>> 60956bd (cleanup scalajs module, improve typesafety)
  )
