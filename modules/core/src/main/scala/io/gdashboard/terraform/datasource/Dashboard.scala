package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.{Element, Primitive, Schema}

final case class Dashboard(
    title: String,
    description: Option[String],
    uid: Option[String],
    version: Option[Int],
    editable: Option[Boolean],
    style: Option[String],
    graphTooltip: Option[String],
    tags: List[String],
    time: Option[Dashboard.TimeOptions],
    variables: List[Variable],
    layout: Option[Dashboard.Layout]
)

object Dashboard {

  final case class TimeOptions(
      refreshLiveDashboards: Option[Boolean],
      timezone: Option[String],
      weekStart: Option[String],
      defaultRange: Option[Time],
      picker: Option[Picker]
  )

  final case class Picker(
      hide: Option[Boolean],
      nowDelay: Option[String],
      refreshIntervals: List[String],
      timeOptions: List[String]
  )

  final case class Layout(sections: List[Dashboard.Section])

  final case class Section(
      title: Option[String],
      collapsed: Option[Boolean],
      panels: List[Panel],
      rows: List[SectionRow]
  )

  final case class SectionRow(
      panels: List[Panel]
  )

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

  implicit val pickerSchema: Schema.Block[Picker] = Schema
    .block[Picker]("picker")
    .addOpt("hide", _.hide)
    .addOpt("now_delay", _.nowDelay)
    .add("refresh_intervals", _.refreshIntervals)
    .add("time_options", _.timeOptions)
    .build

  implicit val timeOptionsSchema: Schema.Block[TimeOptions] = Schema
    .block[TimeOptions]("time")
    .addOpt("timezone", _.timezone)
    .addOpt("week_start", _.weekStart)
    .addOpt("refresh_live_dashboard", _.refreshLiveDashboards)
    .addOpt("default_range", _.defaultRange)
    .addOpt("picker", _.picker)
    .build

  implicit val panelSchema: Schema.Block[Panel] = Schema
    .block[Panel]("panel")
    .add(_.size)
    .add("source", _.source)(Schema.rawStringSchema)
    .build

  implicit val sectionRowSchema: Schema.Block[SectionRow] = Schema
    .block[SectionRow]("row")
    .add("panel", _.panels)(Schema.listInlineBlockSchema)
    .build

  implicit val sectionSchema: Schema.Block[Section] = Schema
    .block[Section]("section")
    .addOpt("title", _.title)
    .addOpt("collapsed", _.collapsed)
    .add("panel", _.panels)(Schema.listInlineBlockSchema)
    .add("rows", _.rows)(Schema.listInlineBlockSchema)
    .build

  implicit val layoutSchema: Schema.Block[Layout] = Schema
    .block[Layout]("layout")
    .add("section", _.sections)(Schema.listInlineBlockSchema)
    .build

  implicit val dashboardSchema: Schema.DataSource[Dashboard] = Schema
    .dataSource[Dashboard]("dashboard")
    .add("title", _.title)
    .addOpt("description", _.description)
    .addOpt("uid", _.uid)
    .addOpt("version", _.version)
    .addOpt("editable", _.editable)
    .addOpt("style", _.style)
    .addOpt("graph_tooltip", _.graphTooltip)
    .add("tags", _.tags)
    .addOpt("time", _.time)
    .add("variables", _.variables)
    .addOpt("layout", _.layout)
    .build
}
