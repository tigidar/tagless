package html.lib

import scala.language.strictEquality

object menu:

  // --- Opaque types (erased to String at runtime; no wrapper allocation) ---
  opaque type MenuKey = String
  object MenuKey:
    inline def apply(s: String): MenuKey = s
    extension (k: MenuKey) inline def value: String = k
    given CanEqual[MenuKey, MenuKey] = CanEqual.derived

  opaque type Title = String
  object Title:
    inline def apply(s: String): Title = s
    extension (t: Title) inline def value: String = t
    given CanEqual[Title, Title] = CanEqual.derived

  opaque type Url = String
  object Url:
    inline def apply(s: String): Url = s
    extension (u: Url) inline def value: String = u
    given CanEqual[Url, Url] = CanEqual.derived

  // --- Data ---
  final case class MenuItem(title: Title, url: Url)

  // Use an immutable, insertion-ordered map as our "OrderedMap"
  type OrderedMap[K, V] = scala.collection.immutable.VectorMap[K, V]

  final case class Menu(items: OrderedMap[MenuKey, MenuItem]):
    def add(key: MenuKey, item: MenuItem): Menu =
      copy(items = items + (key -> item))
    def get(key: MenuKey): Option[MenuItem] = items.get(key)
    def toList: List[(MenuKey, MenuItem)] = items.toList

  object Menu:
    val empty: Menu = Menu(scala.collection.immutable.VectorMap.empty)
    def apply(kvs: (MenuKey, MenuItem)*): Menu =
      Menu(scala.collection.immutable.VectorMap.from(kvs))
