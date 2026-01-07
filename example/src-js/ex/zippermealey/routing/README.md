# Zipper-Mealy Routing Concept

A proof of concept demonstrating URL-based routing for SPAs using:
- **Huet's Zipper** for navigating and updating tree-structured routes
- **Mealy Machines** for modeling state transitions
- **Airstream** for reactive stream processing
- **URI-based Routing** with a declarative DSL
- **Browser History** integration for back/forward navigation

## Conceptual Overview

### The Problem

Web applications have state at multiple granularities:
- **Page-level**: Which route are we on?
- **Component-level**: Is this accordion expanded?
- **Leaf-level**: What color is this button?

Naively modeling this as a flat state machine leads to:
1. Explosion of states (cartesian product of all possibilities)
2. Transitions that need to know about unrelated parts of state
3. Updates that reconstruct entire state trees unnecessarily

### The Solution: Zippers + Mealy Machines + Routing

**Huet's Zipper** (1997) gives us:
- A way to "focus" on a subtree while remembering the context
- O(1) local updates without reconstructing the whole tree
- Navigation operations (up, down, left, right)

**Mealy Machines** give us:
- Clean input → (output, newState) semantics
- Composable state transition logic
- A model that maps directly to streams

**URI-based Routing** gives us:
- URL as ADT (type-safe URL handling)
- Edge-labeled route trees (URL segments = tree edges)
- Route Zipper (navigation with context = URL history)
- Query parameters as typed filters
- Browser history integration

**Combined**, we get:
- Hierarchical state machines that compose
- Focused updates at any level of granularity
- Clean separation between navigation and transition logic
- Bidirectional URL ↔ State mapping

## Project Structure

```
src/main/scala/zippermealey/
├── core/
│   ├── Zipper.scala       # Generic zipper for rose trees
│   ├── Mealy.scala        # Mealy machine abstraction
│   └── Lens.scala         # Simple optics for composition
├── routing/
│   ├── Uri.scala          # URI as ADT
│   ├── RouteTree.scala    # Edge-labeled route trees
│   ├── RouteZipper.scala  # Navigation with context
│   ├── DSL.scala          # Declarative routing DSL
│   ├── Cache.scala        # Entity cache with TTL
│   └── History.scala      # Browser history integration
├── integration/
│   └── AirstreamMealy.scala  # Airstream integration
└── examples/
    ├── Example1_BasicZipper.scala    # Zipper navigation
    ├── Example2_BasicMealy.scala     # Simple state machine
    ├── Example3_Integrated.scala     # Combined approach
    └── Example4_Routing.scala        # Complete routing demo

docs/
├── THEORY.md              # Theoretical foundations
└── ROUTING_DSL.md         # Routing DSL deep dive
```

## The Routing DSL

Define routes declaratively using `~>` for page assignments:

```scala
val userId = ParamDef.long("userId")
val postId = ParamDef.long("postId")

val routes = routes[Page] {
  index ~> HomePage

  "users" / {
    index ~> UserListPage
    userId / {
      index ~> UserProfilePage
      "posts" / {
        index ~> UserPostsPage
        postId ~> PostDetailPage
      }
    }
  }

  "about" ~> AboutPage
}
```

This maps to URLs:
- `/` → HomePage
- `/users` → UserListPage
- `/users/123` → UserProfilePage (userId = 123)
- `/users/123/posts` → UserPostsPage
- `/users/123/posts/456` → PostDetailPage (postId = 456)
- `/about` → AboutPage

## Building and Running

```bash
sbt fastLinkJS
# Or for tests
sbt test
```

## Key Insights

1. **Zippers are Comonads**: The zipper's `extract` gives the focus, 
   `extend` lets you compute based on context.

2. **Mealy Machines are Arrows**: They compose sequentially and in parallel,
   modeling dataflow naturally.

3. **Lenses Bridge Them**: Lenses let you "zoom" a Mealy machine to operate
   on a subpart of state, while the Zipper handles the tree navigation.

4. **URLs are Serialized Zipper Paths**: A URL `/users/123/posts` is just
   a path through the route tree. The Route Zipper makes this bidirectional.

5. **Edge Labels Map to URL Segments**: The route tree has labeled edges
   (literals and parameters), making URL → Route matching natural.

## References

- Huet, G. (1997). "The Zipper"
- Mealy, G. (1955). "A Method for Synthesizing Sequential Circuits"
- Uustalu & Vene (2005). "The Essence of Dataflow Programming"
- Liu, Cheng, Hudak (2009). "Causal Commutative Arrows"
- Harel, D. (1987). "Statecharts: A Visual Formalism for Complex Systems"
