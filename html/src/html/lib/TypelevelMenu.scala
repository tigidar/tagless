package html.lib

import scala.language.strictEquality
import scala.compiletime.{constValue, erasedValue, error as ctError}

// --- Opaques ----------------------------------------------------

opaque type Title = String
object Title:
  inline def apply(s: String): Title = s
  extension (t: Title) inline def value: String = t

opaque type Url = String
object Url:
  inline def apply(s: String): Url = s
  extension (u: Url) inline def value: String = u

type Id[A <: String] = A & Singleton

// --- Items ------------------------------------------------------

final case class MenuItem[Id <: String & Singleton](title: Title, url: Url):
  inline def id: String = constValue[Id]

object MenuItem:
  inline def apply[Id <: String & Singleton](
      title: Title,
      url: Url
  ): MenuItem[Id] =
    new MenuItem[Id](title, url)

// --- Menu with type-level map ----------------------------------

final case class Menu[Items <: Tuple](items: Items):

  /** Compile-time lookup: menu.get["home"] → MenuItem["home"] */
  inline def get[Id <: String & Singleton]: MenuItem[Id] =
    Menu.getImpl[Id, Items](items)

object Menu:

  transparent inline def apply[Items <: Tuple](items: Items): Menu[Items] =
    new Menu(items)

  // inline tuple search over the *type* `Items`
  private inline def getImpl[Id <: String & Singleton, Items <: Tuple](
      items: Items
  ): MenuItem[Id] =
    inline erasedValue[Items] match
      case _: EmptyTuple =>
        ctError("No MenuItem with id " + constValue[Id])

      case _: (MenuItem[Id] *: tail) =>
        // we know by the pattern that `items` is MenuItem[Id] *: tail
        val cons = items.asInstanceOf[MenuItem[Id] *: tail]
        cons.head

      case _: (_ *: tail) =>
        // head is some other MenuItem, skip and recurse on tail
        val cons = items.asInstanceOf[(? *: tail)]
        getImpl[Id, tail](cons.tail)
/*
opaque type Title = String
object Title:
  inline def apply(s: String): Title = s
  extension (t: Title) inline def value: String = t

opaque type Url = String
object Url:
  inline def apply(s: String): Url = s
  extension (u: Url) inline def value: String = u

final case class MenuItem[Id <: String & Singleton](title: Title, url: Url):
  inline def id: String = constValue[Id]

object MenuItem:
  inline def apply[Id <: String & Singleton](
      title: Title,
      url: Url
  ): MenuItem[Id] =
    new MenuItem[Id](title, url)

final case class Menu[Items <: Tuple](items: Items):

  /** Get the item with a given *type-level* id, e.g. menu.get["home"] */
  inline def get[Id <: String & Singleton]: MenuItem[Id] =
    Menu.getImpl[Id, Items](items)

object Menu:

  // inline tuple search
  private inline def getImpl[Id <: String & Singleton, Items <: Tuple](
      items: Items
  ): MenuItem[Id] =
    inline items match
      case _: EmptyTuple =>
        ctError("No MenuItem with id " + constValue[Id])

      // Head is the one we're looking for
      case cons: (MenuItem[Id] *: tail) =>
        cons.head

      // Skip head, recurse on tail
      case cons: (_ *: tail) =>
        getImpl[Id, tail](cons.tail)*/
