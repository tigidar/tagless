package html.lib

import scala.compiletime.constValue

sealed trait Id[Id <: String & Singleton]:
  inline def id: String = constValue[Id]
