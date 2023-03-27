package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.Schema

final case class Table(
    title: String,
    description: Option[String],
    field: Option[FieldOptions],
    graph: Option[Table.Graph],
    overrides: List[FieldOverride],
    queries: List[Query],
    transform: List[Transform]
)

object Table {

  final case class Graph(
      cell: Option[Cell],
      column: Option[Column],
      footer: Option[Footer],
      showHeader: Option[Boolean]
  )

  final case class Cell(
      displayMode: Option[String],
      inspectable: Option[Boolean]
  )

  final case class Column(
      align: Option[String],
      filterable: Option[Boolean],
      minWidth: Option[Int],
      width: Option[Int]
  )

  final case class Footer(
      calculations: List[String],
      fields: List[String],
      pagination: Option[Boolean]
  )

  implicit val cellSchema: Schema.Block[Cell] = Schema
    .block[Cell]("cell")
    .addOpt("display_mode", _.displayMode)
    .addOpt("inspectable", _.inspectable)
    .build

  implicit val columnSchema: Schema.Block[Column] = Schema
    .block[Column]("column")
    .addOpt("align", _.align)
    .addOpt("filterable", _.filterable)
    .addOpt("min_width", _.minWidth)
    .addOpt("width", _.width)
    .build

  implicit val footerSchema: Schema.Block[Footer] = Schema
    .block[Footer]("footer")
    .add("calculations", _.calculations)
    .add("fields", _.fields)
    .addOpt("pagination", _.pagination)
    .build

  implicit val graphSchema: Schema.Block[Graph] = Schema
    .block[Graph]("graph")
    .addOpt("cell", _.cell)
    .addOpt("column", _.column)
    .addOpt("footer", _.footer)
    .addOpt("show_header", _.showHeader)
    .build

  implicit val timeseriesSchema: Schema.DataSource[Table] = Schema
    .dataSource[Table]("table")
    .add("title", _.title)
    .addOpt("description", _.description)
    .addOpt("field", _.field)
    .addOpt("graph", _.graph)
    .add("overrides", _.overrides)
    .add("queries", _.queries)
    .add("transform", _.transform)
    .build

}
