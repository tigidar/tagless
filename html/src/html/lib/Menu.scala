package html.lib

import scala.language.strictEquality
import scala.compiletime.{constValue, error => compiletimeError}

object oldmenu:

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
  final case class MenuItem[Id <: String & Singleton](title: Title, url: Url):
    inline def id: String = constValue[Id]

  object MenuItem:
    inline def create[Id <: String & Singleton](
        inline id: Id,
        url: String
    ): MenuItem[Id] =
      MenuItem[Id](Title(id), Url(url))

  // Use an immutable, insertion-ordered map as our "OrderedMap"
  type OrderedMap[K, V] = scala.collection.immutable.VectorMap[K, V]

  final case class Menu(items: OrderedMap[MenuKey, MenuItem["menu"]]):
    def add(key: MenuKey, item: MenuItem["menu"]): Menu =
      copy(items = items + (key -> item))
    def get(key: MenuKey): Option[MenuItem["menu"]] = items.get(key)
    def toList: List[(MenuKey, MenuItem["menu"])] = items.toList

  object Menu:
    val empty: Menu = Menu(scala.collection.immutable.VectorMap.empty)
    def apply(kvs: (MenuKey, MenuItem["menu"])*): Menu =
      Menu(scala.collection.immutable.VectorMap.from(kvs))

    /*
    transparent inline def makeMenu[Ids <: Tuple](inline ids: Ids): Menu[Ids] =
      Menu(loop(ids))

    private inline def loop[Ids <: Tuple](inline ids: Ids): List[MenuItem[?]] =
      inline ids match
        case EmptyTuple => Nil
        case id *: tail =>
          val item = MenuItem[id.type](Title(id), Url("/" + id))
          item :: loop(tail)

    inline def createMenu(inline ids: String*): List[MenuItem[?]] =
      inline ids match
        case _: EmptyTuple =>
          Nil
        case id *: tail =>
          // id has singleton type here
          val item = MenuItem[id.type](Title(id), Url("/" + id))
          item :: createMenu(tail*)
     */
