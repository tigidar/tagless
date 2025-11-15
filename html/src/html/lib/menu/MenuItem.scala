package html.lib.menu
import html.lib.{IdList, INil, ICons, IdMap}

import scala.language.strictEquality
import scala.compiletime.{constValue, erasedValue, error as ctError}

sealed trait IdValue[Id <: String & Singleton]:
  inline def id: String = constValue[Id]

opaque type Title = String
object Title:
  inline def apply(s: String): Title = s
  extension (t: Title) inline def value: String = t

opaque type Url = String
object Url:
  inline def apply(s: String): Url = s
  extension (u: Url) inline def value: String = u

final case class MenuItem[A <: String & Singleton](title: Title, url: Url)
    extends IdValue[A]

inline def t1 = MenuItem(Title("Home"), Url("/"))

object MenuItem:
  inline def create[A <: String & Singleton](url: Url): MenuItem[A] =
    val title: Title = Title(constValue[A])
    MenuItem[A](title, url)

  println(t1.id)
