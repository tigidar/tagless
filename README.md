# Tagless

Maybe you are bored of reading thousands of (React?) tags generated from AI tools?

Or maybe just save a few AI tokens :D

The answer might be to `tag` less ..

This is a simple tool to write html snippets using a dsl in Scala 3.

This `very much` a `WIP` and highly experimental project!!

Instead of reactive html tag manipulation in the dom, we create one html page via statically typed scala. We use mostly `css` in combination with the frontend streaming library `Airstream` to create reactive web pages using css toggling. Look in the `example` (very `wip`) how this could look like.

```scala
import md.Markdown
// Simple home page with a simple markdown dsl in Scala 3
object Home {

  val content = Markdown.^
    // title <h1>
    <# "Welcome to the TODO App!" 
    // A paragraph <p>
    < "This is a simple TODO application built with Scala.js and a custom markdown DSL." 
    // h2
    <## "Features" //<h2>
    // bullet point - disc style
    <* "Add new TODO items"
    <* "Mark items as completed"
    <* "Delete items"
    // h2
    <## "Getting Started"
    // paragraph
    < "To get started, simply clone the repository and follow the instructions in the README file."
    // h3
    <### "Installation"
    // - dashed bullet points
    <-- "Ensure you have Scala and Scala.js set up on your machine."
    <-- "Clone the repository from GitHub."
    <-- "Run the build tool to compile and start the application."
    // h3
    <### "Usage"
    // paragraph
    < "Open your web browser and navigate to `http://localhost:5173` to access the TODO app."
    // h2
    <## "Contributing"
    // paragraph
    < "Feel free to fork the repository and submit pull requests. Contributions are welcome!"

}

// tagless dsl available by
import tagless.dsl.*

// Create a simple tagless tree, i.e. called a fragment, basic operators
// ~ initiates the dsl
// >> add a child node / dom element
//  > add a sibling to the existing dom element in focus
// <^ 1 move focus to the parent element
// <^ 2 move focus to grandparent etc.
def headBlock(titleText: String, colorScheme: ColorTheme) =
  ~ headTag // `~` initiates the Cursor in the Zipper
    >>  metaTag(charset := "UTF-8") // add a child
      > metaTag(                    // add a sibling to the child
        nameAttr := "viewport",
        contentAttr := "width=device-width, initial-scale=1.0"
      )
      > titleTag(titleText) // add more siblings ...
      > linkTag(rel := "icon", tpe := "image/svg+xml", href := "/vite.svg")
      > linkTag(rel := "apple-touch-icon", href := "/apple-touch-icon.png")
      > linkTag(rel:= "stylesheet", href := "/css/modern-normalize.css")
      > linkTag(rel:= "stylesheet", href := "/css/flatten.css")
      > linkTag(rel:= "stylesheet", href := "/css/layout.css")
      > linkTag(rel:= "stylesheet", href := "/css/styles.css")
      > colorSchemeMeta(colorScheme)

// We can use map to easily transform list of A into tags:
def navItems(menu: Menu) =
  ~ ul(idAttr := "menu-items")
    >> menu.items.map: // List of menu items
       case (key, u) => 
         ~ li( idAttr := s"menu-${key}") // We need to create a new fragment
            >> a(u.title.value, href := u.url.value)


// When we want to use these fragments, we can combine them using the 
// >>^ and >^ operators. This way we do not descend down into the dom tree.
// Below, we will have a body tag as parent and all the other fragmens and normal tags
// will remain siblings, all first child of the body tag.
// The fragments will remain as siblings because we use >>^
val body = 
~ bodyTag // `~` initiates the Cursor in the Zipper
  >>^ menuBlock(menu) // `>>^` Add a fragment as a child, do not descend into it
  >> div(idAttr := "app") // `>>` Add a div with id "app" as a child, descend into it
  > div(idAttr := "welcome-content") // `>` Add a sibling to the tag above
  >>^ Home.content // `>>^` Add markdown content as a child, focus is kept at existing level
  > scriptTag( tpe := "module", src := "/src/main.ts") // `>` Add a sibling script tag
```

## Licesning

This project is licensed under the Apache License 2.0.
It includes components licensed under the MIT License:

- tags (Copyright 2017 Nikita Gazarov)

`tags` is taken from:

https://github.com/raquo/scala-dom-types

It is slithly modified in this project.

## How To

This project uses [mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) as a build tool.

Current version of mill is: `v1.0.5`. It should automatically download needed version from the already installed mill version.

#### IDE Setup

In essence `vscode` with the `metals` plugin and `intellij` with the `scala plugin` are supported out of the box, you should not need to do anything special. There is more information here: [mill bsp](https://mill-build.org/mill/cli/installation-ide.html#_explicit_bsp_installation)

#### NixOS specifics

If you are on a nixos system, you need to run:

```bash
mill --bsp-install
```

It will generate a json that points to the nix wrapped binary you need to run mill.

#### Manual IDE setup (IntelliJ)

For more sophisticated environment, you can also use :

```bash
mill --bsp-install
```

For IntelliJ IDEA, you can create a xml project specification by:

```bash
mill mill.idea
```

You might however need to add this as a plugin depending on your mill version.

## Testing the example app, works as a dev example

The example app showcases some experimental ideas on how to use the library. Primarily we create the html as a single page containing everything using the dom dsl. Then we use `Airstream` to handle reactivity, i.e. all events in the dom is captured and transforms the display via css toggling. To keep track of the state we utilise Scala's typesystem.

### Generate the html code

This simply runs the dsl and write its output to `index.html` in the web folder:

```bash
export SCALA_VERSION=3.7.2
mill -w example["$SCALA_VERSION"].jvm.run
```

### Compile ScalaJS code

To compile the scalajs code, run:

```bash
export SCALA_VERSION=3.7.2
mill -w example["$SCALA_VERSION"].js.compile
```

To generate the javascript code, run:

```bash
# dev mode
mill -w example["$SCALA_VERSION"].js.fastLinkJS
```

If you want production code, highly optimized, run:

```bash
# release mode
mill -w example["$SCALA_VERSION"].js.fullLinkJS
```

### Link in scalajs code into the web app

To simplify access to generated ScalaJS Javascript code, make this symlink:

```bash
ln -sfnT "$(realpath out/example/js/3.7.2/fastLinkJS.dest)" example/web/src/scala
```

Not sure about the T option, it might only be needed if the folder already exists to avoid putting the symlink inside the folder.

### Set up Javascript dev server

Remember to install the `vite` project dependencies:

```bash
cd example/web
npm install
```

Then run the vite dev server:

```bash
npm run dev
```

Then open `example/web/src/html/index.html` in a browser.


## Modules

### tags

Mostly a customized, generated from [scala-dom-types](https://github.com/raquo/scala-dom-types), all credits to them.

### dom

A Cursor based dom / tree builder using the Zipper pattern from the paper `Functional Pearl: The Zipper, written by G. Huet`

### markdown

A simple dsl to generate dom fragments to be used in the dsl

### dsl

A simple dsl to simplify manipulation of the Zipper / Cursor based dom builder.

### example

A simple example how to use the dsl to create a static page and integrate with `Airstream` for reactivity.

## Future improvements

- Add proper docs
- Support `xhtml` attributes / markup
- Make things more typesafe
- Better integration with `Airstream`
- Better ScalaJS browser optimalization
