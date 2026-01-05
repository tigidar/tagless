package html.lib

import scala.compiletime.constValue

trait Id[Id <: String & Singleton]:
  inline def id: String = constValue[Id]

inline def id[Value <: String & Singleton]: Value & Singleton =
  constValue[Value]

/*
object Id:
  inline def apply(using n: sourcecode.FullName): Id[n.value.type] =
    new Id[n.value.type] {}

inline def id(using n: sourcecode.FullName): Id[n.value.type] =
  new Id[n.value.type] {}
 */
