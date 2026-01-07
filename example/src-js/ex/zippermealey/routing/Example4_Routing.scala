package zippermealey.examples

import zippermealey.routing.*
import com.raquo.airstream.ownership.Owner
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.*

/**
 * =============================================================================
 * EXAMPLE: COMPLETE ROUTING SYSTEM DEMONSTRATION
 * =============================================================================
 * 
 * This example shows the full integration of:
 *   - URI as ADT
 *   - Route tree with labeled edges
 *   - Route zipper for navigation
 *   - Query parameters as typed filters
 *   - Entity cache with TTL
 *   - Declarative DSL
 *   - Browser history integration
 * 
 * =============================================================================
 * SCENARIO: A Blog Application
 * =============================================================================
 * 
 * Routes:
 *   /                          → Home
 *   /users                     → User list (with ?sort, ?filter)
 *   /users/:userId             → User profile
 *   /users/:userId/posts       → User's posts
 *   /users/:userId/posts/:postId → Single post
 *   /posts                     → All posts (with ?tag, ?page)
 *   /posts/:postId             → Single post (alternative route)
 *   /about                     → About page
 *   /settings                  → Settings
 *   /settings/profile          → Profile settings
 *   /settings/notifications    → Notification settings
 */
object Example4_Routing:
  
  // ===========================================================================
  // DOMAIN TYPES
  // ===========================================================================
  
  // Strongly typed IDs (not just String/Long)
  opaque type UserId = Long
  object UserId:
    def apply(id: Long): UserId = id
    given Read[UserId] = Read[Long].map(UserId(_))
    given Show[UserId] = Show[Long].contramap(identity)
    extension (id: UserId) def value: Long = id
  
  opaque type PostId = Long
  object PostId:
    def apply(id: Long): PostId = id
    given Read[PostId] = Read[Long].map(PostId(_))
    given Show[PostId] = Show[Long].contramap(identity)
    extension (id: PostId) def value: Long = id
  
  // Domain entities
  case class User(id: UserId, name: String, email: String)
  case class Post(id: PostId, authorId: UserId, title: String, content: String, tags: List[String])
  
  // Query filter types
  case class UserFilters(
    sort: Option[String],
    filter: Option[String]
  )
  
  case class PostFilters(
    tag: Option[String],
    page: Option[Int]
  )
  
  // ===========================================================================
  // PAGE TYPES (What gets rendered)
  // ===========================================================================
  
  enum Page:
    case Home
    case UserList(filters: UserFilters)
    case UserProfile(userId: UserId)
    case UserPosts(userId: UserId)
    case PostDetail(postId: PostId, context: PostContext)
    case AllPosts(filters: PostFilters)
    case About
    case Settings
    case ProfileSettings
    case NotificationSettings
    case NotFound(path: String)
  
  enum PostContext:
    case Standalone      // /posts/:postId
    case UserContext(userId: UserId)  // /users/:userId/posts/:postId
  
  // ===========================================================================
  // PARAMETER DEFINITIONS
  // ===========================================================================
  
  // These define the dynamic segments in our URLs
  val userId = ParamDef.long("userId")
  val postId = ParamDef.long("postId")
  
  // ===========================================================================
  // QUERY CODECS
  // ===========================================================================
  
  val userFiltersCodec: QueryCodec[UserFilters] = new QueryCodec[UserFilters]:
    def decode(query: Query) = Right(UserFilters(
      sort = query.get("sort"),
      filter = query.get("filter")
    ))
    def encode(filters: UserFilters) = 
      var q = Query.empty
      filters.sort.foreach(s => q = q.set("sort", s))
      filters.filter.foreach(f => q = q.set("filter", f))
      q
  
  val postFiltersCodec: QueryCodec[PostFilters] = new QueryCodec[PostFilters]:
    def decode(query: Query) = Right(PostFilters(
      tag = query.get("tag"),
      page = query.get("page").flatMap(_.toIntOption)
    ))
    def encode(filters: PostFilters) =
      var q = Query.empty
      filters.tag.foreach(t => q = q.set("tag", t))
      filters.page.foreach(p => q = q.set("page", p.toString))
      q
  
  // ===========================================================================
  // ROUTE TREE DEFINITION (Using DSL)
  // ===========================================================================
  
  /**
   * Here's the beautiful declarative DSL in action!
   * 
   * Notice how it reads almost like documentation:
   */
  val routeTree: RouteNode[Page] = routes[Page] {
    // Root: Home page
    index -> Page.Home
    
    // /users branch
    "users" / {
      index -> Page.UserList(UserFilters(None, None))
      
      // /users/:userId branch
      userId / {
        index -> Page.UserProfile(UserId(0)) // Placeholder, real ID from params
        
        // /users/:userId/posts branch
        "posts" / {
          index -> Page.UserPosts(UserId(0))
          
          // /users/:userId/posts/:postId
          postId -> Page.PostDetail(PostId(0), PostContext.Standalone)
        }
      }
    }
    
    // /posts branch
    "posts" / {
      index -> Page.AllPosts(PostFilters(None, None))
      
      // /posts/:postId
      postId -> Page.PostDetail(PostId(0), PostContext.Standalone)
    }
    
    // /about (leaf)
    "about" -> Page.About
    
    // /settings branch
    "settings" / {
      index -> Page.Settings
      "profile" -> Page.ProfileSettings
      "notifications" -> Page.NotificationSettings
    }
  }
  
  // ===========================================================================
  // DEMONSTRATION CODE
  // ===========================================================================
  
  def demo(): Unit =
    println("=" * 70)
    println("EXAMPLE 4: COMPLETE ROUTING SYSTEM")
    println("=" * 70)
    
    // -------------------------------------------------------------------------
    // Part 1: URI as ADT
    // -------------------------------------------------------------------------
    println("\n1. URI AS ADT")
    println("-" * 50)
    
    // Construct URIs type-safely
    val uri1 = Uri.root / "users" / "123" / "posts"
    println(s"   Constructed: ${uri1.render}")
    
    val uri2 = Uri("users", "123", "posts")
      .withQuery("sort", "date")
      .withQuery("order", "desc")
      .withFragment("latest")
    println(s"   With query & fragment: ${uri2.render}")
    
    // Parse URIs
    val parsed = Uri.parse("/users/456/posts?tag=scala&page=2#comments")
    parsed.foreach { uri =>
      println(s"   Parsed path: ${uri.path.render}")
      println(s"   Parsed query: ${uri.query.toMap}")
      println(s"   Parsed fragment: ${uri.fragment.map(_.value)}")
    }
    
    // URI operations
    val uri3 = Uri.root / "users"
    println(s"\n   Parent of ${uri3.render}: ${uri3.parent.map(_.render)}")
    println(s"   Is root? ${Uri.root.path.isRoot}")
    
    // -------------------------------------------------------------------------
    // Part 2: Route Tree & Edge Labels
    // -------------------------------------------------------------------------
    println("\n2. ROUTE TREE STRUCTURE")
    println("-" * 50)
    
    def printTree(node: RouteNode[Page], indent: Int = 0, edgeLabel: String = "ROOT"): Unit =
      val prefix = "  " * indent
      val pageStr = node.page.map(_.toString.take(30)).getOrElse("(no page)")
      println(s"$prefix[$edgeLabel] → $pageStr")
      node.children.toList.sortBy(_._1.render).foreach { case (edge, child) =>
        printTree(child, indent + 1, edge.render)
      }
    
    println("   Route tree (edge labels shown):")
    printTree(routeTree, 1)
    
    // -------------------------------------------------------------------------
    // Part 3: Route Zipper Navigation
    // -------------------------------------------------------------------------
    println("\n3. ROUTE ZIPPER NAVIGATION")
    println("-" * 50)
    
    val zipper = RouteZipper.fromRoot(routeTree)
    println(s"   Starting at root: ${zipper.currentUri.render}")
    println(s"   Available edges: ${zipper.availableEdges.map(_.render)}")
    
    // Navigate down
    val atUsers = zipper.goDown("users").get
    println(s"\n   After goDown('users'): ${atUsers.currentUri.render}")
    println(s"   Has page? ${atUsers.hasPage}")
    println(s"   Depth: ${atUsers.depth}")
    
    // Navigate with parameter
    val atUser123 = atUsers.goDown("123").get
    println(s"\n   After goDown('123'): ${atUser123.currentUri.render}")
    println(s"   Path params: ${atUser123.pathParams}")
    
    // Navigate deeper
    val atUserPosts = atUser123.goDown("posts").get
    println(s"\n   After goDown('posts'): ${atUserPosts.currentUri.render}")
    println(s"   Breadcrumbs: ${atUserPosts.breadcrumbs.map(_._1)}")
    
    // Navigate to specific post
    val atPost456 = atUserPosts.goDown("456").get
    println(s"\n   After goDown('456'): ${atPost456.currentUri.render}")
    println(s"   All path params: ${atPost456.pathParams}")
    
    // Navigate back up
    val backUp = atPost456.goUp.get.goUp.get
    println(s"\n   After 2x goUp: ${backUp.currentUri.render}")
    
    // Navigate from URI
    val fromUri = RouteZipper.fromUri(routeTree, Uri.parse("/settings/profile").toOption.get)
    println(s"\n   Navigate to /settings/profile: ${fromUri.map(_.currentUri.render)}")
    println(s"   Page: ${fromUri.flatMap(_.currentPage)}")
    
    // -------------------------------------------------------------------------
    // Part 4: Query Parameters
    // -------------------------------------------------------------------------
    println("\n4. QUERY PARAMETERS")
    println("-" * 50)
    
    val usersWithQuery = atUsers
      .setQuery("sort", "name")
      .setQuery("filter", "active")
    println(s"   Added query params: ${usersWithQuery.currentUri.render}")
    println(s"   Query map: ${usersWithQuery.query.toMap}")
    
    // Decode typed filters
    val decodedFilters = userFiltersCodec.decode(usersWithQuery.query)
    println(s"   Decoded as UserFilters: $decodedFilters")
    
    // Encode back
    val encoded = userFiltersCodec.encode(UserFilters(Some("date"), Some("published")))
    println(s"   Encoded filters: ${encoded.render}")
    
    // Remove query params
    val withoutSort = usersWithQuery.removeQuery("sort")
    println(s"   After removing 'sort': ${withoutSort.currentUri.render}")
    
    // -------------------------------------------------------------------------
    // Part 5: Router & Matching
    // -------------------------------------------------------------------------
    println("\n5. ROUTER & URL MATCHING")
    println("-" * 50)
    
    val router = Router(routeTree)
    
    val testUris = List(
      "/",
      "/users",
      "/users/42",
      "/users/42/posts",
      "/users/42/posts/99",
      "/posts",
      "/posts/99",
      "/about",
      "/settings/notifications",
      "/nonexistent"
    )
    
    testUris.foreach { uriStr =>
      val result = router.matchUriString(uriStr)
      val status = result match
        case Right(RouteResult.Matched(route)) => 
          s"✓ ${route.page.toString.take(40)}"
        case Right(RouteResult.NotFound(_, partial)) =>
          s"✗ Not found (partial: ${partial.isDefined})"
        case Left(err) =>
          s"✗ Parse error: $err"
      println(s"   $uriStr → $status")
    }
    
    // -------------------------------------------------------------------------
    // Part 6: Sibling Navigation
    // -------------------------------------------------------------------------
    println("\n6. SIBLING NAVIGATION")
    println("-" * 50)
    
    val atAbout = RouteZipper.fromUri(routeTree, Uri.parse("/about").toOption.get).get
    println(s"   At: ${atAbout.currentUri.render}")
    println(s"   Can go left? ${atAbout.canGoUp && atAbout.goUp.get.goDown("posts").isDefined}")
    
    val atUsersAgain = zipper.goDown("users").get
    val atPosts = atUsersAgain.goRight
    println(s"   From /users, go right: ${atPosts.map(_.currentUri.render)}")
    
    // -------------------------------------------------------------------------
    // Part 7: Cache System
    // -------------------------------------------------------------------------
    println("\n7. ENTITY CACHE")
    println("-" * 50)
    
    given ExecutionContext = scala.concurrent.ExecutionContext.global
    
    // Create a cache
    val userCache = EntityCache.inMemory[UserId, User](ttl = 30.seconds)
    
    // Simulate some cached data
    val user1 = User(UserId(1), "Alice", "alice@example.com")
    val user2 = User(UserId(2), "Bob", "bob@example.com")
    
    userCache.put(UserId(1), user1)
    userCache.put(UserId(2), user2)
    
    println(s"   Cache stats after puts: ${userCache.stats}")
    println(s"   Get user 1: ${userCache.get(UserId(1)).map(_.name)}")
    println(s"   Get user 3 (miss): ${userCache.get(UserId(3))}")
    println(s"   Cache stats after gets: ${userCache.stats}")
    
    // -------------------------------------------------------------------------
    // Part 8: Fluent API Alternative
    // -------------------------------------------------------------------------
    println("\n8. ALTERNATIVE FLUENT API")
    println("-" * 50)
    
    val fluentRoutes = Route.root[String]
      .child("api")
        .child("v1")
          .child("users")
            .leaf("Users endpoint")
          .end
        .end
      .end
      .child("docs")
        .leaf("Documentation")
      .end
      .build
    
    println("   Fluent-built route tree:")
    printTree(fluentRoutes.asInstanceOf[RouteNode[Page]], 1)
    
    // -------------------------------------------------------------------------
    // Summary
    // -------------------------------------------------------------------------
    println("\n" + "=" * 70)
    println("SUMMARY: URL ↔ ZIPPER ↔ STATE")
    println("=" * 70)
    println("""
    |   ┌─────────────────────────────────────────────────────────────┐
    |   │                     URL (String)                            │
    |   │        /users/123/posts?sort=date#comments                  │
    |   └───────────────────────┬─────────────────────────────────────┘
    |                           │ parse / render
    |   ┌───────────────────────▼─────────────────────────────────────┐
    |   │                      URI (ADT)                              │
    |   │   Uri(Path([users, 123, posts]), Query(sort→date), #...)   │
    |   └───────────────────────┬─────────────────────────────────────┘
    |                           │ match against RouteTree
    |   ┌───────────────────────▼─────────────────────────────────────┐
    |   │                   RouteZipper                               │
    |   │   focus: UserPosts node                                     │
    |   │   context: [users, :userId(123), posts]                     │
    |   │   params: {userId: 123}                                     │
    |   │   query: {sort: date}                                       │
    |   └───────────────────────┬─────────────────────────────────────┘
    |                           │ extract page + params
    |   ┌───────────────────────▼─────────────────────────────────────┐
    |   │                    Page + State                             │
    |   │   Page.UserPosts(userId=123, filters=PostFilters(...))     │
    |   └─────────────────────────────────────────────────────────────┘
    |   
    |   Navigation is BIDIRECTIONAL:
    |     • URL → Zipper → Page (routing)
    |     • Page → Zipper → URL (link generation)
    |     • Zipper operations → History API (browser sync)
    """.stripMargin)
    
    println("=" * 70)
  
  def main(args: Array[String]): Unit = demo()

// ===========================================================================
// HELPER EXTENSIONS FOR DEMO
// ===========================================================================

extension [A](read: Read[A])
  def map[B](f: A => B): Read[B] = new Read[B]:
    def read(s: String) = read.read(s).map(f)

extension [A](show: Show[A])
  def contramap[B](f: B => A): Show[B] = b => show.show(f(b))
