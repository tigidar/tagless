package ex

import html.lib.id

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
  )
