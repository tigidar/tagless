package ex

import html.lib.id

object PageMap:

  object NavButtons:
    val homeId: "home-button" = id["home-button"]
    val aboutId: "about-button" = id["about-button"]
    val todoId: "todo-button" = id["todo-button"]
    val contactId: "contact-button" = id["contact-button"]

  object Pages:
    val welcomeId: "welcome-content" = id["welcome-content"]
    val aboutId: "about-content" = id["about-content"]

    val pageIds = Set(
      welcomeId,
      aboutId
    )
