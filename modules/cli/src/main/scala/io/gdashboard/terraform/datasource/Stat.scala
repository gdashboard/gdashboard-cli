package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.Schema

final case class Stat(
    title: String,
    description: Option[String],
    field: Option[FieldOptions],
    graph: Option[Stat.Graph],
    overrides: List[FieldOverride],
    queries: List[Query],
    transform: List[Transform]
)

object Stat {

  final case class Graph(
      colorMode: Option[String],
      graphMode: Option[String],
      orientation: Option[String],
      textAlignment: Option[String],
      textMode: Option[String],
      textSize: Option[TextSize],
      options: Option[ReduceOptions]
  )

  implicit val graphSchema: Schema.Block[Graph] = Schema
    .block[Graph]("graph")
    .addOpt("color_mode", _.colorMode)
    .addOpt("graph_mode", _.graphMode)
    .addOpt("orientation", _.orientation)
    .addOpt("text_alignment", _.textAlignment)
    .addOpt("text_mode", _.textMode)
    .addOpt("text_size", _.textSize)
    .addOpt("options", _.options)
    .build

  implicit val timeseriesSchema: Schema.DataSource[Stat] = Schema
    .dataSource[Stat]("stat")
    .add("title", _.title)
    .addOpt("description", _.description)
    .addOpt("field", _.field)
    .addOpt("graph", _.graph)
    .add("overrides", _.overrides)
    .add("queries", _.queries)
    .add("transform", _.transform)
    .build

}
