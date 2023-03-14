package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.{Element, Primitive, Schema}

final case class Dashboard(
    title: String,
    uid: Option[String],
    editable: Option[Boolean],
    style: Option[String],
    graphTooltip: Option[String],
    time: Option[Time],
    variables: List[Variable],
    layout: Option[Dashboard.Layout]
)

object Dashboard {

  final case class Layout(rows: List[Dashboard.Row])

  final case class Row(panels: List[Panel])

  final case class Panel(source: String, size: Size)

  sealed trait Size

  object Size {
    final case class Ref(reference: String)         extends Size
    final case class Fixed(height: Int, width: Int) extends Size

    implicit val refSizeSchema: Schema[Ref] =
      (name: String, a: Ref) => Element.Attribute(name, Primitive.RawStr(a.reference))

    implicit val fixedSizeSchema: Schema.Block[Fixed] = Schema
      .block[Fixed]("size =")
      .add("height", _.height)
      .add("width", _.width)
      .build

    implicit val sizeSchema: Schema.Block[Size] =
      new Schema.Block[Size] {
        def toElement(size: Size): Element =
          size match {
            case ref: Ref     => Schema[Ref].toElement("size", ref)
            case fixed: Fixed => Schema.Block[Fixed].toElement("size =", fixed)
          }

        def toElement(name: String, size: Size): Element =
          size match {
            case ref: Ref     => Schema[Ref].toElement(name, ref)
            case fixed: Fixed => Schema.Block[Fixed].toElement(name, fixed)
          }
      }
  }

  implicit val panelSchema: Schema.Block[Panel] = Schema
    .block[Panel]("panel")
    .add(_.size)
    .add("source", _.source)(Schema.rawStringSchema)
    .build

  implicit val rowSchema: Schema.Block[Row] = Schema
    .block[Row]("row")
    .add("panel", _.panels)(Schema.listInlineBlockSchema)
    .build

  implicit val layoutSchema: Schema.Block[Layout] = Schema
    .block[Layout]("layout")
    .add("row", _.rows)(Schema.listInlineBlockSchema)
    .build

  implicit val dashboardSchema: Schema.DataSource[Dashboard] = Schema
    .dataSource[Dashboard]("dashboard")
    .add("title", _.title)
    .addOpt("uid", _.uid)
    .addOpt("editable", _.editable)
    .addOpt("style", _.style)
    .addOpt("graph_tooltip", _.graphTooltip)
    .addOpt("time", _.time)
    .add("variables", _.variables)
    .addOpt("layout", _.layout)
    .build
}
