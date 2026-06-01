package eventhandler.router

import scala.scalajs.js

/** Algebraic data type representing navigation events.
  *
  * These events are emitted by the Router when navigation occurs, whether
  * triggered programmatically or by user browser actions (back/forward buttons,
  * hash changes).
  *
  * ==Usage==
  *
  * {{{
  * import eventhandler.router.{Router, RouteEvent}
  *
  * val router = Router.create
  *
  * router.navigationEvents.foreach {
  *   case RouteEvent.Push(path, _) =>
  *     println(s"Navigated to: $path")
  *     renderPage(path)
  *
  *   case RouteEvent.Pop(path, _) =>
  *     println(s"User went back to: $path")
  *     renderPage(path)
  *
  *   case RouteEvent.Replace(path, _) =>
  *     println(s"Replaced URL with: $path")
  *
  *   case RouteEvent.HashChange(_, newHash) =>
  *     scrollToAnchor(newHash)
  * }
  * }}}
  */
enum RouteEvent:

  /** Navigation via history.pushState.
    *
    * @param path
    *   The new path (e.g., "/about")
    * @param state
    *   Optional state object passed to pushState
    */
  case Push(path: String, state: Option[js.Any])

  /** Navigation via browser back/forward (popstate event).
    *
    * @param path
    *   The path navigated to
    * @param state
    *   State object from history entry, if present
    */
  case Pop(path: String, state: Option[js.Any])

  /** URL replacement via history.replaceState.
    *
    * @param path
    *   The new path
    * @param state
    *   Optional state object passed to replaceState
    */
  case Replace(path: String, state: Option[js.Any])

  /** Hash fragment change (hashchange event).
    *
    * @param oldHash
    *   Previous hash (without #)
    * @param newHash
    *   New hash (without #)
    */
  case HashChange(oldHash: String, newHash: String)

object RouteEvent:

  extension (event: RouteEvent)
    /** Get the path from any RouteEvent. */
    def path: String = event match
      case Push(p, _)       => p
      case Pop(p, _)        => p
      case Replace(p, _)    => p
      case HashChange(_, h) => s"#$h"

    /** Get the state from navigation events, if present. */
    def state: Option[js.Any] = event match
      case Push(_, s)    => s
      case Pop(_, s)     => s
      case Replace(_, s) => s
      case _             => None

    /** Check if this is a hash-only change. */
    def isHashChange: Boolean = event match
      case HashChange(_, _) => true
      case _                => false
