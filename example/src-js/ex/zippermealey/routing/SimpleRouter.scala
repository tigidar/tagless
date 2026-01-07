package zippermealey.routing

import com.raquo.airstream.core.{Signal, Observer}
import com.raquo.airstream.state.Var
import com.raquo.airstream.ownership.Owner
import org.scalajs.dom
import org.scalajs.dom.window

/**
 * A simplified router for basic URL ↔ Page synchronization.
 *
 * This is a lightweight alternative to the full RouterStore when you only need:
 *   - URL path → Page mapping
 *   - Browser history back/forward
 *   - Shareable URLs
 *
 * It doesn't include:
 *   - Query parameters
 *   - Parameterized routes (:id)
 *   - Entity caching
 *   - Route tree navigation (zipper)
 */
object SimpleRouter:

  /** A simple route mapping from URL path to page ID */
  final case class SimpleRoute(
    path: String,     // URL path like "/about"
    buttonId: String, // Button ID like "about-button"
    elementId: String // Content element ID like "about-content"
  )

  /** Configuration for the simple router */
  final case class RouterConfig(
    routes: List[SimpleRoute],
    defaultRoute: SimpleRoute,
    notFoundRoute: Option[SimpleRoute] = None,
    mode: HistoryMode = HistoryMode.Path
  ):
    /** Find a route by URL path */
    def findByPath(path: String): Option[SimpleRoute] =
      val normalized = if path.isEmpty || path == "/" then "/" else path.stripSuffix("/")
      routes.find(r => normalize(r.path) == normalized)

    /** Find a route by button ID */
    def findByButtonId(buttonId: String): Option[SimpleRoute] =
      routes.find(_.buttonId == buttonId)

    private def normalize(path: String): String =
      if path.isEmpty || path == "/" then "/" else path.stripSuffix("/")

  /**
   * Create a reactive router that syncs URL with page state.
   *
   * @param config Router configuration
   * @param onNavigate Callback when navigation happens (to trigger DOM updates)
   */
  final class ReactiveRouter(
    config: RouterConfig,
    onNavigate: SimpleRoute => Unit
  )(using owner: Owner):

    // Current route state
    private val currentRouteVar: Var[SimpleRoute] = Var(resolveInitialRoute())

    // Signal of current route
    val currentRoute: Signal[SimpleRoute] = currentRouteVar.signal

    // Listen to browser popstate (back/forward buttons)
    private val popstateListener: scala.scalajs.js.Function1[dom.PopStateEvent, Unit] = { _ =>
      val path = readCurrentPath()
      config.findByPath(path).orElse(config.notFoundRoute).foreach { route =>
        currentRouteVar.set(route)
        onNavigate(route)
      }
    }
    window.addEventListener("popstate", popstateListener)

    /** Navigate to a route by path */
    def navigateToPath(path: String): Unit =
      config.findByPath(path) match
        case Some(route) =>
          pushState(path)
          currentRouteVar.set(route)
          onNavigate(route)
        case None =>
          config.notFoundRoute.foreach { route =>
            currentRouteVar.set(route)
            onNavigate(route)
          }

    /** Navigate to a route by button ID */
    def navigateToButton(buttonId: String): Unit =
      config.findByButtonId(buttonId).foreach { route =>
        pushState(route.path)
        currentRouteVar.set(route)
        onNavigate(route)
      }

    /** Get the current URL path */
    def currentPath: String = readCurrentPath()

    /** Go back in browser history */
    def back(): Unit = window.history.back()

    /** Go forward in browser history */
    def forward(): Unit = window.history.forward()

    // Internal helpers
    private def readCurrentPath(): String =
      config.mode match
        case HistoryMode.Path => window.location.pathname
        case HistoryMode.Hash =>
          val hash = window.location.hash.stripPrefix("#")
          if hash.isEmpty then "/" else hash

    private def pushState(path: String): Unit =
      val url = config.mode match
        case HistoryMode.Path => path
        case HistoryMode.Hash => s"#$path"
      window.history.pushState(null, "", url)

    private def resolveInitialRoute(): SimpleRoute =
      val path = readCurrentPath()
      config.findByPath(path)
        .orElse(config.notFoundRoute)
        .getOrElse(config.defaultRoute)

  /**
   * Quick helper to create routes from common patterns.
   */
  object Routes:
    /** Create a route where buttonId is derived from path */
    def simple(path: String, elementId: String): SimpleRoute =
      val buttonId = path.stripPrefix("/").replace("/", "-") + "-button"
      SimpleRoute(path, if buttonId == "-button" then "home-button" else buttonId, elementId)

    /** Create a home route */
    def home(elementId: String): SimpleRoute =
      SimpleRoute("/", "home-button", elementId)

    /** Create a standard set of routes from path -> elementId pairs */
    def fromPairs(pairs: (String, String)*): List[SimpleRoute] =
      pairs.map { case (path, elementId) => simple(path, elementId) }.toList
