# Routing DSL: Deep Dive

This document explains the design principles and theory behind the routing DSL.

## Table of Contents

1. [The Core Insight](#1-the-core-insight)
2. [URI as ADT](#2-uri-as-adt)
3. [Edge-Labeled Route Trees](#3-edge-labeled-route-trees)
4. [The Route Zipper](#4-the-route-zipper)
5. [The DSL Design](#5-the-dsl-design)
6. [Query Parameters as State](#6-query-parameters-as-state)
7. [Entity Cache with TTL](#7-entity-cache-with-ttl)
8. [Browser History Integration](#8-browser-history-integration)
9. [Putting It All Together](#9-putting-it-all-together)

---

## 1. The Core Insight

**A URL is a serialized path through a tree.**

```
URL:  /users/123/posts/456

Tree path:
    Root
     │
     └─["users"]─→ Users
                    │
                    └─[":userId"=123]─→ UserProfile
                                        │
                                        └─["posts"]─→ UserPosts
                                                      │
                                                      └─[":postId"=456]─→ PostDetail
```

This insight unifies several concepts:
- **URL segments** = Edge labels in the tree
- **Current URL** = Path from root to current node
- **URL parameters** = Captured values on parameterized edges
- **Navigation** = Moving through the tree (which changes the URL)

The Zipper then becomes the natural representation:
- **Focus**: The current route node
- **Context**: The path we took (= the URL segments)
- **Operations**: goDown/goUp map to URL changes

---

## 2. URI as ADT

### Why Not Strings?

Strings are the source of countless bugs:
```scala
// Easy to mess up
val url = "/users/" + userId + "/posts?sort=" + sort + "&filter=" + filter
// Forgot to encode? Typo in path? Missing slash? Runtime errors await.
```

### The ADT Approach

```scala
// Type-safe construction
val uri = Uri.root / "users" / userId / "posts"
  .withQuery("sort", sort)
  .withQuery("filter", filter)

// Pattern matching for routing
uri.path.segments match
  case PathSegment.Literal("users") :: PathSegment.Captured("userId", id) :: rest => ...
```

### URI Components

```
/path/segments?query=params&more=values#fragment
│             │                        │
Path          Query                    Fragment
│             │                        │
Navigation    Filter/Sort State        In-page anchor
(tree edges)  (ephemeral, shareable)   (client-only)
```

Each component has a distinct role:
- **Path**: Determines WHICH page/resource
- **Query**: Modifies HOW to display it (filters, pagination, sorting)
- **Fragment**: WHERE on the page (section, comment)

---

## 3. Edge-Labeled Route Trees

### Why Edges, Not Nodes?

Consider: "What does 'users' mean?"

In a node-labeled tree:
```
     [users]
     /     \
  [123]   [456]
```
The node "users" doesn't tell us the URL. We need to look at the path.

In an edge-labeled tree:
```
      ●────"users"────→ ●────":userId"────→ ●
```
Each edge IS a URL segment. The URL is just the concatenation of edges.

### Edge Types

```scala
enum EdgeLabel:
  case Literal(value: String)      // "users", "posts", "about"
  case Param(name: String, ...)    // ":userId", ":postId"
  case Wildcard                    // "*" (catch-all)
```

Matching priority: `Literal > Param(with validation) > Param(without) > Wildcard`

This ensures `/users/new` matches the literal "new" before the param `:userId`.

---

## 4. The Route Zipper

### Structure

```scala
case class RouteZipper[Page](
  focus: RouteNode[Page],      // Current node
  context: RouteContext[Page], // How we got here
  query: Query,                // Current query params
  fragment: Option[Fragment]   // Current fragment
)
```

### Context as Breadcrumb Trail

```scala
case class RouteCrumb[Page](
  edge: EdgeLabel,             // The edge we traversed
  capturedValue: Option[String], // If param, the captured value
  parentNode: RouteNode[Page], // The node we came from
  leftSiblings: List[EdgeLabel],  // Sibling edges to the left
  rightSiblings: List[EdgeLabel]  // Sibling edges to the right
)
```

### Bidirectionality

The zipper enables bidirectional transformation:

```
URL String          URI ADT           RouteZipper          Page + Params
     │                 │                   │                    │
     │    parse        │    navigate       │    extract         │
     ├───────────────►├─────────────────►├──────────────────►│
     │                 │                   │                    │
     │    render       │  currentUri       │   construct        │
     │◄───────────────│◄─────────────────│◄──────────────────│
```

---

## 5. The DSL Design

### Design Goals

1. **Readability**: Route definitions should be self-documenting
2. **Type Safety**: Parameters are typed, not stringly-typed
3. **Bidirectionality**: Same definition for matching AND generating URLs
4. **Composability**: Build complex routes from simple parts

### The DSL

Note: The DSL uses `~>` instead of `->` to avoid conflicts with Scala's tuple syntax.

```scala
val routes = routes[Page] {
  // Root index
  index ~> HomePage

  // Literal segment with children
  "users" / {
    index ~> UserListPage

    // Parameterized segment
    userId / {
      index ~> UserProfilePage

      "posts" / {
        index ~> UserPostsPage
        postId ~> PostDetailPage
      }
    }
  }

  // Simple leaf route
  "about" ~> AboutPage
}
```

### How It Works

The DSL uses Scala 3's context functions:

```scala
def routes[Page](block: RouteBuilder[Page] ?=> Unit): RouteNode[Page]

extension (segment: String)
  def /[Page](block: RouteBuilder[Page] ?=> Unit)(using parent: RouteBuilder[Page]): Unit
  def ->[Page](page: Page)(using parent: RouteBuilder[Page]): Unit
```

The `RouteBuilder` is passed implicitly, accumulating route definitions as you write them.

### Parameter Definitions

```scala
// Define once, use everywhere
val userId = ParamDef.long("userId")  // Parses to Long, validates as numeric
val postId = ParamDef.long("postId")
val slug = ParamDef.string("slug")

// Custom validation
val uuid = ParamDef.string("id").validate(ParamValidator.uuid)
```

---

## 6. Query Parameters as State

### Philosophy

Query parameters represent **ephemeral, shareable state**:
- Filters: `?status=active&role=admin`
- Pagination: `?page=3&limit=20`
- Sorting: `?sort=name&order=desc`
- UI state: `?modal=settings&tab=security`

This state:
- Should be shareable via URL
- Should survive page refresh
- Should NOT be in the path (it doesn't identify a resource)

### Typed Query Codecs

```scala
case class UserFilters(
  status: Option[String],
  role: Option[String],
  sort: Option[String]
)

val userFiltersCodec: QueryCodec[UserFilters] = ...

// Decode from query
val filters = userFiltersCodec.decode(uri.query)

// Encode to query
val query = userFiltersCodec.encode(UserFilters(Some("active"), None, Some("name")))
```

### Query vs Path

| Aspect | Path | Query |
|--------|------|-------|
| Purpose | Identify resource | Modify view of resource |
| Semantics | Hierarchical | Key-value pairs |
| Caching | Different path = different resource | Same path, different query = same resource |
| Example | `/users/123` (which user) | `?sort=name` (how to display) |

---

## 7. Entity Cache with TTL

### Why Cache?

URLs contain IDs, not data:
```
/users/123   ← Just an ID
             ↓ Fetch
User(id=123, name="Alice", ...)  ← Actual data
```

Without caching:
- Every navigation fetches
- Duplicate requests for same entity
- Poor user experience

### Cache Design

```scala
trait EntityCache[K, V]:
  def get(key: K): Option[V]          // None if missing or expired
  def put(key: K, value: V): Unit     // With default TTL
  def put(key: K, value: V, ttl: FiniteDuration): Unit
```

Features:
- **TTL**: Entries expire after a duration
- **LRU eviction**: Removes oldest entries when at capacity
- **Request deduplication**: Concurrent requests for same key share one fetch

### Integration with Routes

```scala
// Register loaders
routerStore.registerLoader("users", userLoader)
routerStore.registerLoader("posts", postLoader)

// In page rendering, use the cache
val user = routerStore.loader[UserId, User]("users")
  .flatMap(_.load(userId))
```

---

## 8. Browser History Integration

### The History API

The browser provides:
```javascript
history.pushState(state, title, url)   // New history entry
history.replaceState(state, title, url) // Update current entry
window.onpopstate = (event) => { ... } // User navigated
```

### Our Integration

```
┌─────────────────────────────────────────────────────────────┐
│                    RouterStore                              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ RouteZipper │◄──►│   History   │◄──►│  Browser    │     │
│  └─────────────┘    └─────────────┘    │  History API│     │
│        │                                └─────────────┘     │
│        ▼                                                    │
│  currentUri, currentPage, pathParams (Signals)             │
└─────────────────────────────────────────────────────────────┘
```

Navigation modes:
- **Path mode**: Real URLs (`/users/123`) - requires server config
- **Hash mode**: Hash URLs (`/#/users/123`) - works everywhere

### Navigation Commands

```scala
// Programmatic navigation (pushes history)
routerStore.navigateTo(Uri.parse("/users/123"))
routerStore.goDown("posts")
routerStore.goUp()

// Query/fragment changes (replaces history, no new entry)
routerStore.setQuery("sort", "name")
routerStore.setFragment("comments")

// Browser navigation
routerStore.back()
routerStore.forward()
```

---

## 9. Putting It All Together

### Complete Flow

```
User clicks link to /users/123/posts?sort=date
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  1. PARSE: String → URI ADT                             │
│     Uri(Path([users, 123, posts]), Query(sort→date))   │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  2. MATCH: URI → RouteZipper                            │
│     Navigate: root →[users]→ ●  →[:userId=123]→ ●      │
│                              →[posts]→ ● (focus)       │
│     Capture: {userId: 123}                              │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  3. EXTRACT: Zipper → Page + Params                     │
│     Page: UserPostsPage                                 │
│     Params: userId = 123                                │
│     Query: PostFilters(sort = Some("date"))            │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  4. FETCH: ID → Entity (via cache)                      │
│     userLoader.load(123) → User(id=123, name="Alice")  │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  5. RENDER: Page + Data → UI                            │
│     UserPostsPage(user, posts, filters)                │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│  6. HISTORY: Update browser URL                         │
│     history.pushState(null, "", "/users/123/posts...")  │
└─────────────────────────────────────────────────────────┘
```

### Example Application Setup

```scala
// 1. Define your pages
enum Page:
  case Home, UserList, UserProfile(userId: UserId), ...

// 2. Define parameters
val userId = ParamDef.long("userId")

// 3. Define routes with DSL
val routeTree = routes[Page] {
  index ~> Page.Home
  "users" / {
    index ~> Page.UserList
    userId / {
      index ~> Page.UserProfile(UserId(0)) // Placeholder
    }
  }
}

// 4. Create router store (handles history, caching, etc.)
given Owner = ...
val store = RouterStore(routeTree, HistoryMode.Path)

// 5. Register entity loaders
store.registerLoader("users", CachedLoader(fetchUser, ttl = 5.minutes))

// 6. Use in your UI
store.currentPage.foreach {
  case Page.UserProfile(_) =>
    val userId = store.zipper.now().param[UserId]("userId")
    // Render user profile...
  case ...
}
```

---

## Appendix: DSL Quick Reference

```scala
// Route definition (note: use ~> instead of ->)
routes[Page] {
  index ~> HomePage              // Index page for this level
  "literal" ~> LeafPage          // Literal leaf route
  "literal" / { ... }            // Literal with children
  paramDef ~> LeafPage           // Param leaf route
  paramDef / { ... }             // Param with children
}

// Parameter definitions
val id = ParamDef.long("id")           // Long parameter
val uuid = ParamDef.uuid("id")         // UUID parameter
val slug = ParamDef.string("slug")     // String parameter
val custom = ParamDef[MyType]("name")  // Custom type (needs Read/Show)

// Validation
val id = ParamDef.string("id").numeric     // Must be numeric
val id = ParamDef.string("id").uuid        // Must be UUID
val id = ParamDef.string("id").validate(myValidator)

// Navigation
zipper.goDown("segment")    // Navigate to child
zipper.goUp                 // Navigate to parent
zipper.goLeft / goRight     // Navigate to siblings
zipper.goToRoot             // Navigate to root
zipper.navigateTo(uri)      // Navigate to specific URI

// Query manipulation
zipper.setQuery("key", "value")
zipper.removeQuery("key")
zipper.clearQuery

// URL generation
zipper.currentUri           // Get current URI
router.uriFor(page, params) // Generate URI for page
```
