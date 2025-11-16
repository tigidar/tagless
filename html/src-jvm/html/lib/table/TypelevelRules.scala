package html.lib.table

import scala.annotation.implicitNotFound

sealed trait TableElement

sealed trait AddColGroup extends TableElement
sealed trait AddHeader extends TableElement
sealed trait AddBody extends TableElement
sealed trait AddFooter extends TableElement

@implicitNotFound(
  "Cannot add a ${Elem} in current state ${S}. Allowed states for ${Elem} are: ${Allowed}."
)
trait CanAdd[S <: TableBuilderState, Elem <: TableElement]:
  type Allowed <: TableBuilderState
  type Result <: TableBuilderState

object CanAdd:
  type Aux[
      S <: TableBuilderState,
      Elem <: TableElement,
      A <: TableBuilderState,
      R <: TableBuilderState
  ] =
    CanAdd[S, Elem] {
      type Allowed = A
      type Result = R
    }

  // ColGroup
  given colGroupFromStart
      : Aux[TStart, AddColGroup, TStart | TCaption, TColgroup] =
    new CanAdd[TStart, AddColGroup]:
      type Allowed = TStart | TCaption
      type Result = TColgroup

  given colGroupFromCaption
      : Aux[TCaption, AddColGroup, TStart | TCaption, TColgroup] =
    new CanAdd[TCaption, AddColGroup]:
      type Allowed = TStart | TCaption
      type Result = TColgroup

  // Header
  given headerFromStart
      : Aux[TStart, AddHeader, TStart | TCaption | TColgroup, THeader] =
    new CanAdd[TStart, AddHeader]:
      type Allowed = TStart | TCaption | TColgroup
      type Result = THeader

  given headerFromCaption
      : Aux[TCaption, AddHeader, TStart | TCaption | TColgroup, THeader] =
    new CanAdd[TCaption, AddHeader]:
      type Allowed = TStart | TCaption | TColgroup
      type Result = THeader

  given headerFromColGroup
      : Aux[TColgroup, AddHeader, TStart | TCaption | TColgroup, THeader] =
    new CanAdd[TColgroup, AddHeader]:
      type Allowed = TStart | TCaption | TColgroup
      type Result = THeader

  // Body
  given bodyFromStart
      : Aux[TStart, AddBody, TStart | TCaption | TColgroup | THeader, TBody] =
    new CanAdd[TStart, AddBody]:
      type Allowed = TStart | TCaption | TColgroup | THeader
      type Result = TBody

  given bodyFromCaption
      : Aux[TCaption, AddBody, TStart | TCaption | TColgroup | THeader, TBody] =
    new CanAdd[TCaption, AddBody]:
      type Allowed = TStart | TCaption | TColgroup | THeader
      type Result = TBody

  given bodyFromColGroup: Aux[
    TColgroup,
    AddBody,
    TStart | TCaption | TColgroup | THeader,
    TBody
  ] =
    new CanAdd[TColgroup, AddBody]:
      type Allowed = TStart | TCaption | TColgroup | THeader
      type Result = TBody

  given bodyFromHeader
      : Aux[THeader, AddBody, TStart | TCaption | TColgroup | THeader, TBody] =
    new CanAdd[THeader, AddBody]:
      type Allowed = TStart | TCaption | TColgroup | THeader
      type Result = TBody

  given footerFromBody: Aux[
    TBody,
    AddFooter,
    TStart | TCaption | TColgroup | THeader | TBody,
    TFooter
  ] =
    new CanAdd[TBody, AddFooter]:
      type Allowed = TStart | TCaption | TColgroup | THeader | TBody
      type Result = TFooter

// format: off
/**
  * Simplified version of the above, however not able to provide detailed
  * error messages.
trait CanAddColGroup[S]

object CanAddColGroup:
  given canAddGroupForStart: CanAddColGroup[TStart] with {}
  given canAddGroupForCaption: CanAddColGroup[TCaption] with {}

trait CanAddHeader[S]

object CanAddHeader:
  given canAddHeaderForColGroup: CanAddHeader[TColgroup] with {}
  given canAddHeaderForStart: CanAddHeader[TStart] with {}
  given canAddHeaderForCaption: CanAddHeader[TCaption] with {}

trait CanAddBody[S]

object CanAddBody:
  given canAddBodyForHeader: CanAddBody[THeader] with {}
  given canAddBodyForColGroup: CanAddBody[TColgroup] with {}
  given canAddBodyForStart: CanAddBody[TStart] with {}
  given canAddBodyForCaption: CanAddBody[TCaption] with {}
*/
