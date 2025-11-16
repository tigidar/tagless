package html.lib

final case class ErrorMessage(
    title: String = "Error",
    message: String = """An unexpected error has occurred.
        Please try again later or contact support if the problem persists.""",
)
