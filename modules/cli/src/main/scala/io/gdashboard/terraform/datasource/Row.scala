package io.gdashboard.terraform
package datasource

import io.gdashboard.terraform.ast.Schema

final case class Row(
    title: String,
    graph: Option[Row.Graph]
)

object Row {

  final case class Graph(collapsed: Option[Boolean])

  implicit val graphSchema: Schema.Block[Graph] = Schema
    .block[Graph]("graph")
    .addOpt("collapsed", _.collapsed)
    .build

  implicit val rowSchema: Schema.DataSource[Row] = Schema
    .dataSource[Row]("row")
    .add("title", _.title)
    .addOpt("graph", _.graph)
    .build

}
