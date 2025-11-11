package ex

import md.Markdown

object About {
  val content = Markdown.^
    <# "About the TODO App"
    < "The TODO App is designed to help users manage their tasks efficiently. It is built using Scala.js and leverages a custom markdown DSL for content creation."
    <## "Key Features"
    <* "User-friendly interface"
    <* "Real-time updates"
    <* "It will have persistent storage of tasks"
    <## "Technologies Used"
    <* "Scala.js for frontend development"
    <* "Custom markdown DSL for content formatting"
    <* "HTML and CSS for styling"
    <## "Contact"
    < "For more information or support, please contact the development team at"

}
