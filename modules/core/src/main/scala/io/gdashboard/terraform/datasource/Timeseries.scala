package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.Schema

final case class Timeseries(
    title: String,
    description: Option[String],
    field: Option[FieldOptions],
    axis: Option[Axis],
    graph: Option[Timeseries.Graph],
    legend: Option[Legend],
    overrides: List[FieldOverride],
    queries: List[Query],
    tooltip: Option[Tooltip],
    transform: List[Transform]
)

object Timeseries {

  final case class Graph(
      drawStyle: Option[String],
      fillOpacity: Option[Int],
      gradientMode: Option[String],
      lineInterpolation: Option[String],
      lineStyle: Option[String],
      lineWidth: Option[Int],
      pointSize: Option[Int],
      showPoints: Option[String],
      spanNulls: Option[Boolean],
      spanStackSeries: Option[String]
  )

  implicit val graphSchema: Schema.Block[Graph] = Schema
    .block[Graph]("graph")
    .addOpt("draw_style", _.drawStyle)
    .addOpt("fill_opacity", _.fillOpacity)
    .addOpt("gradient_mode", _.gradientMode)
    .addOpt("line_interpolation", _.lineInterpolation)
    .addOpt("line_style", _.lineStyle)
    .addOpt("line_width", _.lineWidth)
    .addOpt("point_size", _.pointSize)
    .addOpt("show_points", _.showPoints)
    .addOpt("span_nulls", _.spanNulls)
    .addOpt("stack_series", _.spanStackSeries)
    .build

  implicit val timeseriesSchema: Schema.DataSource[Timeseries] = Schema
    .dataSource[Timeseries]("timeseries")
    .add("title", _.title)
    .addOpt("description", _.description)
    .addOpt("field", _.field)
    .addOpt("axis", _.axis)
    .addOpt("graph", _.graph)
    .addOpt("legend", _.legend)
    .add("overrides", _.overrides)
    .add("queries", _.queries)
    .addOpt("tooltip", _.tooltip)
    .add("transform", _.transform)
    .build

}
