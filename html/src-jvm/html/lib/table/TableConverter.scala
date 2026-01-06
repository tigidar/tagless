package html.lib.table

import dom.{Cursor, Node, NodeType, NormalType}
import Cursor.{*, given}
import tags.T.*
import tags.gen.Element
import tags.keys.HtmlAttr
import tags.codecs.StringAsIsCodec

object TableConverter:
  import dom.CursorExtensions.{*, given}

  // The span attribute for col elements (not in scala-dom-types by default)
  private val spanAttr: HtmlAttr[String] = HtmlAttr("span", StringAsIsCodec)

  extension [N1 <: NodeType, E1 <: Element](childCur: Cursor[N1, E1])
    private def upAsParent[Np <: NodeType, Ep <: Element]: Cursor[Np, Ep] =
      childCur.up.asInstanceOf[Cursor[Np, Ep]]

  private def withChild[
      N <: NodeType,
      E <: Element,
      N1 <: NodeType,
      E1 <: Element
  ](
      c: Cursor[N, E],
      child: Node[N1, E1]
  )(
      f: Cursor[N1, E1] => Cursor[N1, E1]
  )(using ev: N =:= NormalType): Cursor[N, E] =
    val down: Cursor[N1, E1] = c.addChild(child)
    val edited: Cursor[N1, E1] = f(down)
    edited.upAsParent[N, E]

  def build[S <: TableBuilderState](t: Table[S]): Cursor[NormalType, tags.gen.HTMLTableElement] =
    val tableCursor: Cursor[NormalType, tags.gen.HTMLTableElement] = Cursor(table)
    convert(t, tableCursor)

  private def convert[S <: TableBuilderState, N <: NodeType, E <: Element](
      t: Table[S],
      c: Cursor[N, E]
  )(using ev: N =:= NormalType): Cursor[N, E] =
    t match
      case Table.Init =>
        c

      case Table.Caption(value) =>
        c.addChildStay(caption(value))

      case Table.ColGroup(columns) =>
        withChild(c, colGroup) { cg =>
          columns.foldLeft(cg) { (cur, column) =>
            column.spanNrOfColumns match
              case Some(s) => cur.addChildStay(col(spanAttr := s))
              case None    => cur.addChildStay(col)
          }
        }

      case Table.Head(rows, captionOpt) =>
        val withCaption = captionOpt.fold(c)(cap => c.addChildStay(caption(cap.value)))
        withChild(withCaption, thead) { th =>
          rows.foldLeft(th) { (cur, hrow) =>
            withChild(cur, tr) { trCur =>
              hrow.columns.foldLeft(trCur) { (trC, cell) =>
                trC.addChildStay(tags.T.th(cell.content))
              }
            }
          }
        }

      case Table.Body(rows, headOpt, captionOpt) =>
        val withCaption = captionOpt.fold(c)(cap => c.addChildStay(caption(cap.value)))
        val withHead = headOpt.fold(withCaption)(h => convertHead(h, withCaption))
        withChild(withHead, tbody) { tb =>
          rows.foldLeft(tb) { (cur, row) =>
            withChild(cur, tr) { trCur =>
              row.cells.foldLeft(trCur) { (trC, cell) =>
                trC.addChildStay(td(cell.content))
              }
            }
          }
        }

      case Table.Footer(text, bodyOpt, headOpt, captionOpt) =>
        val withCaption = captionOpt.fold(c)(cap => c.addChildStay(caption(cap.value)))
        val withHead = headOpt.fold(withCaption)(h => convertHead(h, withCaption))
        val withBody = bodyOpt.fold(withHead)(b => convertBody(b, withHead))
        withChild(withBody, tfoot) { tf =>
          withChild(tf, tr) { trCur =>
            trCur.addChildStay(td(text))
          }
        }

  private def convertHead[N <: NodeType, E <: Element](
      h: Table.Head,
      c: Cursor[N, E]
  )(using ev: N =:= NormalType): Cursor[N, E] =
    withChild(c, thead) { th =>
      h.rows.foldLeft(th) { (cur, hrow) =>
        withChild(cur, tr) { trCur =>
          hrow.columns.foldLeft(trCur) { (trC, cell) =>
            trC.addChildStay(tags.T.th(cell.content))
          }
        }
      }
    }

  private def convertBody[N <: NodeType, E <: Element](
      b: Table.Body,
      c: Cursor[N, E]
  )(using ev: N =:= NormalType): Cursor[N, E] =
    withChild(c, tbody) { tb =>
      b.rows.foldLeft(tb) { (cur, row) =>
        withChild(cur, tr) { trCur =>
          row.cells.foldLeft(trCur) { (trC, cell) =>
            trC.addChildStay(td(cell.content))
          }
        }
      }
    }

  extension [S <: TableBuilderState](t: Table[S])
    def toDom: Cursor[NormalType, tags.gen.HTMLTableElement] =
      build(t)

    def toHtml: String =
      build(t).seal.toHtml
