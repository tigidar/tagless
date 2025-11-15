package html.lib

import scala.compiletime.{constValue, error as ctError}

trait HasKey[K <: String & Singleton]:
  def key: K

object HasKey:
  // you *can* keep a trivial alias if you really want an Aux-ish name:
  type Aux[K <: String & Singleton] = HasKey[K]

/*
object HasKey:
  // Aux shorthand if/when you want it
  type Aux[K <: String & Singleton] = HasKey { type Key = K }
 */

final case class Button[K <: String & Singleton](key: K, label: String)
    extends HasKey[K]

final case class Select[K <: String & Singleton](key: K, label: String)
    extends HasKey[K]

final case class MenuI[K <: String & Singleton, C <: HasKey[K]](
    component: C
)

object MenuItem:
  def apply[K <: String & Singleton](
      key: K,
      label: String
  ): MenuI[K, Button[K]] =
    MenuItem(Button(key, label))

val m1 = MenuI["menu1", Button["menu1"]](
  Button("menu1", "Menu 1")
)

sealed trait IdList[F[_ <: String & Singleton]]

final class INil[F[_ <: String & Singleton]] extends IdList[F]

final case class ICons[
    F[_ <: String & Singleton],
    Id <: String & Singleton,
    Tail <: IdList[F]
](item: F[Id], tail: Tail)
    extends IdList[F]

final case class IdMap[F[_ <: String & Singleton], L <: IdList[F]](items: L):

  inline def get[Id <: String & Singleton]: F[Id] =
    IdMap.getImpl[F, Id, L](items)

object IdMap:

  private inline def getImpl[
      F[_ <: String & Singleton],
      Id <: String & Singleton,
      L <: IdList[F]
  ](l: L): F[Id] =
    inline l match
      case _: INil[F] =>
        ctError("No element with id " + constValue[Id])

      case cons: ICons[F, Id, tail] =>
        cons.item // F[Id]

      case cons: ICons[F, otherId, tail] => ???
      // getImpl[F, Id, tail](cons.tail.asInstanceOf[tail])
