# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tagless is a Scala 3 DSL for building HTML documents using a Zipper pattern for tree navigation. It generates static HTML pages and integrates with Airstream for reactivity via CSS toggling.

## Build System

This project uses [Mill](https://mill-build.org/) (v1.0.5) as the build tool. Mill auto-downloads the required version.

### Common Commands

```bash
# Run tests for a module
mill markdown.jvm["3.7.3"].test
mill html.jvm["3.7.3"].test

# Compile a module
mill dom.jvm["3.7.3"].compile

# Generate HTML (runs JVM example)
make example-watch-html
# or: mill -w example.jvm["3.7.3"].run

# Compile Scala.js
make js-example-watch-compile
# or: mill -w example.js["3.7.3"].compile

# Generate JS bundle (dev mode)
make js-example-fastlink
# or: mill example.js["3.7.3"].fastLinkJS

# Production JS bundle
mill example.js["3.7.3"].fullLinkJS

# Symlink JS output to web folder
make link-fast

# Run Vite dev server
cd example/web && npm install && npm run dev
```

### IDE Setup

```bash
mill --bsp-install  # Required for NixOS, recommended for all
mill mill.idea      # Generate IntelliJ project files
```

## Architecture

### Module Structure

- **tags**: HTML/SVG tag definitions (adapted from scala-dom-types, MIT licensed)
- **dom**: Zipper-based tree builder with `Cursor` for navigating/building DOM trees
- **markdown**: DSL for markdown-like content generation
- **dsl**: Main DSL combining dom and markdown capabilities
- **html**: HTML utilities (colors, menus, IDs)
- **example**: Demo app showing DSL usage with Airstream for reactivity

### Core Concepts

**Zipper Pattern**: The `Cursor` type navigates the DOM tree during construction. Context (`Ctx`) stores siblings and parent for rebuilding.

**DSL Operators** (from `tagless.dsl`):
- `~` - Initialize a cursor from a tag
- `>>` - Add child and descend into it
- `>>^` - Add child/fragment, stay at current level
- `>` - Add sibling
- `>^` - Add sibling, stay at current level
- `<^` or `^` - Move up to parent

**Node Types**: `NormalType` (has children), `VoidType` (self-closing), `TextType`

### Cross-Platform

Modules have JVM and JS variants:
- `module.jvm["3.7.3"]` - JVM target
- `module.js["3.7.3"]` - Scala.js target

## Testing

Uses MUnit framework:

```bash
mill <module>.jvm["3.7.3"].test
```

## Licensing

- Overall project: Apache License 2.0
- `tags` submodule: MIT License (from raquo/scala-dom-types)
