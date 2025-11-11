package ex

import md.Markdown

object Home {

  val content = Markdown.^
    <# "Welcome to the TODO App!"
    < "This is a simple TODO application built with Scala.js and a custom markdown DSL."
    <## "Greate features"
    <* "Add new TODO items"
    <* "Mark items as completed"
    <* "Delete items"
    <## "Getting Started"
    < "To get started, simply clone the repository and follow the instructions in the README file."
    <### "Installation"
    <-- "Ensure you have Scala and Scala.js set up on your machine."
    <-- "Clone the repository from GitHub."
    <-- "Run the build tool to compile and start the application."
    <### "Usage"
    < "Open your web browser and navigate to `http://localhost:5173` to access the TODO app."
    <## "Contributing"
    < "Feel free to fork the repository and submit pull requests. Contributions are welcome!"

}
