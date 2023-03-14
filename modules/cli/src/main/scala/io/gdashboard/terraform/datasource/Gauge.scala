package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.Schema

final case class Gauge(
    title: String,
    description: Option[String],
    field: Option[FieldOptions],
    graph: Option[Gauge.Graph],
    overrides: List[FieldOverride],
    queries: List[Query],
    transform: List[Transform]
)

object Gauge {

  final case class Graph(
      orientation: Option[String],
      showThresholdLabels: Option[Boolean],
      showThresholdMarkers: Option[Boolean],
      textSize: Option[TextSize],
      options: Option[ReduceOptions]
  )

  implicit val graphSchema: Schema.Block[Graph] = Schema
    .block[Graph]("graph")
    .addOpt("orientation", _.orientation)
    .addOpt("show_threshold_labels", _.showThresholdLabels)
    .addOpt("show_threshold_markers", _.showThresholdMarkers)
    .addOpt("text_size", _.textSize)
    .addOpt("options", _.options)
    .build

  implicit val gaugeSchema: Schema.DataSource[Gauge] = Schema
    .dataSource[Gauge]("gauge")
    .add("title", _.title)
    .addOpt("description", _.description)
    .addOpt("field", _.field)
    .addOpt("graph", _.graph)
    .add("overrides", _.overrides)
    .add("queries", _.queries)
    .add("transform", _.transform)
    .build

}
