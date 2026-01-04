# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System

This project uses [Mill](https://mill-build.org/) v1.0.6 as the build tool. Scala version is 3.7.3.

### Common Commands

```bash
# Set Scala version for commands
export SCALA_VERSION=3.7.3

# Run the example app (generates index.html)
mill example.jvm["$SCALA_VERSION"].run

# Compile ScalaJS code
mill example.js["$SCALA_VERSION"].compile

# Generate JS (dev mode)
mill example.js["$SCALA_VERSION"].fastLinkJS

# Generate JS (production, optimized)
mill example.js["$SCALA_VERSION"].fullLinkJS

# Run tests for a specific module
mill dom.jvm["$SCALA_VERSION"].test
mill markdown.jvm["$SCALA_VERSION"].test
mill dsl.jvm["$SCALA_VERSION"].test

# Watch mode (auto-recompile on changes)
mill -w example.jvm["$SCALA_VERSION"].run
```

### IDE Setup

For VSCode with Metals or IntelliJ with Scala plugin, run:
```bash
mill --bsp-install
```

## Architecture

Tagless is a Scala 3 library for building HTML using a type-safe DSL based on the Zipper pattern.

### Module Structure

- **tags**: HTML/SVG element definitions, attribute types, and setters. Derived from scala-dom-types (MIT licensed).
- **dom**: Core Zipper/Cursor implementation for navigating and building DOM trees. Contains `Node`, `Tree`, `Cursor`, and `Ctx` types.
- **markdown**: DSL for writing markdown-style content that renders to DOM fragments.
- **dsl**: Main API entry point (`tagless.dsl.*`). Provides operators for tree manipulation.
- **html**: Reusable HTML components (menus, tables, themes). JVM-only extensions in `html.lib`.
- **html-js-extensions**: ScalaJS-specific extensions with Airstream integration.
- **example**: Demo app showing JVM HTML generation + ScalaJS reactivity with Airstream.

### Core Concepts

**Zipper Pattern**: The `Cursor` type (`dom/src/dom/Dom.scala`) implements a functional zipper for navigating and building trees. It maintains a focus node and a stack of `Ctx` (context) records containing left/right siblings and parent.

**DSL Operators** (from `dsl/src/tagless/dsl.scala`):
- `~` - Initiate a cursor from a tag
- `>>` - Add child and descend into it
- `>>^` - Add child/fragment and stay at current level
- `>` - Add sibling and move focus to it
- `>^` - Add sibling and stay at current level
- `<^` / `^` - Navigate up to parent(s)

**Node Types**: The type system tracks whether elements are `NormalType` (can have children) or `VoidType` (self-closing). This prevents invalid operations at compile time.

### Example Web App Setup

The example generates static HTML on JVM, then uses ScalaJS + Airstream for client-side reactivity via CSS toggling.

```bash
# Link generated JS into web folder
ln -sfnT "$(realpath out/example/js/3.7.3/fastLinkJS.dest)" example/web/src/scala

# Install and run vite
cd example/web && npm install && npm run dev
```

## Licensing

- Main project: Apache License 2.0
- `tags` module: MIT License (from raquo/scala-dom-types)
