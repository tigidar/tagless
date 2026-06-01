package tags

/** Phantom types for compile-time depth tracking.
  *
  * These types have no runtime representation - they exist only to let the
  * compiler track how deep we are in the tree and prevent invalid operations
  * like ascending past the root.
  */
sealed trait Depth

/** Root level - depth 0. Cannot ascend from here. */
sealed trait D0 extends Depth

/** One level deeper than D. Succ[D0] = depth 1, Succ[Succ[D0]] = depth 2, etc.
  */
sealed trait Succ[D <: Depth] extends Depth

object Depth:
  /** Type aliases for common depths */
  type D1 = Succ[D0]
  type D2 = Succ[D1]
  type D3 = Succ[D2]
  type D4 = Succ[D3]
  type D5 = Succ[D4]
