# tagless

A type-safe HTML DSL family for Scala 3, organized as **14 small focused
modules** so consumers can pull exactly the surface they need.

> ## 🚧 UNDER CONSTRUCTION 🚧
>
> **This project is a work in progress and is not yet usable.** Nothing here is
> guaranteed to work, compile, or stay stable. APIs, types, and documentation
> may change or disappear without notice, and any published coordinates below
> are **not yet released**. Do not depend on this project yet — treat everything
> in this README as a statement of intent, not a promise.

- **Group:** `no.virtual-architect`
- **Version:** `0.1.0-SNAPSHOT`
- **Scala:** 3.8.3 · **Scala.js:** 1.20.1
- **License:** Apache-2.0 (the `core` module derives a small surface from
  `raquo/scala-dom-types`, MIT)

## Modules

| Module | Platforms | Artifact | Depends on | Purpose |
|--------|-----------|----------|------------|---------|
| `htmlid` | JVM, JS | `tagless-htmlid` | — | Typesafe `HtmlId`, `IdScope`, `RouteScope` |
| `core` | JVM, JS | `tagless-core` | `htmlid`, `i18n` | Cursor/Tag/Node DSL, attrs, classes, Fragment, render |
| `i18n` | JVM, JS | `tagless-i18n` | — | `Lang`, `I18n`, `TextScope` |
| `md` | JVM, JS | `tagless-md` | `core`, `i18n` | Wiki-style Markdown DSL |
| `meta` | JVM, JS | `tagless-meta` | `core` | Convenience builders for `<meta>` tags |
| `page` | JVM, JS | `tagless-page` | `core`, `i18n`, `meta` | High-level Page composition |
| `form` | JVM, JS | `tagless-form` | `core`, `i18n` | Type-state Form DSL (JS adds `FormPopulator`) |
| `table` | JVM, JS | `tagless-table` | `core`, `i18n` | Type-state Table DSL |
| `crud` | JVM, JS | `tagless-crud` | `core`, `i18n`, `form` | View/edit CRUD scaffolding |
| `route` | JVM, JS | `tagless-route` | `core` | `asRoute` extension + `RouteExtractor` |
| `viz` | JVM, JS | `tagless-viz` | `core` | Tree visualization (ASCII / D3 / Mermaid) + `asComponent` |
| `htmx` | JVM, JS | `tagless-htmx` | `core` | HTMX attribute extensions |
| `svg` | JVM, JS | `tagless-svg` | `core` | SVG tags & attributes |
| `events` | **JS only** | `tagless-events` | `htmlid` (+ Airstream) | Document-level DOM event handling |

### Module-name encoding

On-disk paths are kebab-cased; Mill objects are camelCased; published
artifacts use `tagless-<kebab>`. All three coincide here because every
module is single-word.

| on-disk | Mill object | artifactName |
|---------|-------------|--------------|
| `core` | `core` | `tagless-core` |
| `htmlid` | `htmlid` | `tagless-htmlid` |
| `i18n` | `i18n` | `tagless-i18n` |
| `md` | `md` | `tagless-md` |
| `meta` | `meta` | `tagless-meta` |
| `page` | `page` | `tagless-page` |
| `form` | `form` | `tagless-form` |
| `table` | `table` | `tagless-table` |
| `crud` | `crud` | `tagless-crud` |
| `route` | `route` | `tagless-route` |
| `viz` | `viz` | `tagless-viz` |
| `htmx` | `htmx` | `tagless-htmx` |
| `svg` | `svg` | `tagless-svg` |
| `events` | `events` | `tagless-events` |

## Build

```bash
# Devshell with Mill + JDK
nix develop

# Resolve every Cross variant of every module
mill resolve __

# Compile everything (JVM + JS)
mill __.compile

# Compile a specific module/platform
mill core.jvm[3.8.3].compile
mill events.js[3.8.3].compile

# Test a single module
mill md.jvm[3.8.3].test.testForked

# Test everything (use the explicit task name; `__.test` trips a Mill
# resolver bug because not every module has a test sub-object)
for m in core md meta page form table htmx svg viz; do
  mill "$m.jvm[3.8.3].test.testForked"
done

# Link a JS module
mill events.js[3.8.3].fastLinkJS
mill events.js[3.8.3].fullLinkJS   # optimized

# Publish-local SNAPSHOT to ~/.ivy2/local for downstream consumption
mill __.publishLocal
```

## Consuming from another Mill project

Once published locally, depend on any module by Maven coordinate:

```scala
def mvnDeps = super.mvnDeps() ++ Seq(
  mvn"no.virtual-architect::tagless-core::0.1.0-SNAPSHOT",
  mvn"no.virtual-architect::tagless-form::0.1.0-SNAPSHOT",
  mvn"no.virtual-architect::tagless-i18n::0.1.0-SNAPSHOT"
)
```

For Scala.js, the same coordinates resolve to the `_sjs1` artifacts
automatically when used inside a `ScalaJSModule`.

## Package naming note

The build preserves package declarations exactly as they were in the
source monolith. Three modules use packages that **do not match** their
on-disk module name — this is intentional, not a build accident:

| Module | Package |
|--------|---------|
| `md` | `md` |
| `form` | `html.lib.form` |
| `table` | `html.lib.table` |

A future cleanup pass may harmonize these to `tags.md`, `tags.form`,
`tags.table` for consistency with the rest of the ecosystem.

## Related repos

Broken out of the same monolithic source and consuming `tagless`:

| Repo | Purpose | Depends on |
|------|---------|------------|
| [shapesdsl](https://github.com/tigidar/shapesdsl) | Shape ADT + heatmap + SVG interpreter | `tagless-svg` |

## Layout convention

Each module follows this layout:

```
<module>/src/             — shared sources (always present)
<module>/src-jvm/         — JVM-only sources (only where divergence exists)
<module>/src-js/          — JS-only sources
<module>/test/src/        — shared test sources
<module>/jvm/             — empty Cross-variant moduleDir
<module>/js/              — empty Cross-variant moduleDir
```

`build.mill` wires `<module>/src/` in via `Task.Sources(moduleDir / os.up / "src")`.
