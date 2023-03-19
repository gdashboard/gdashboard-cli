package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class ReduceOptions(
    calculation: Option[String],
    fields: Option[String],
    limit: Option[Int],
    values: Option[Boolean]
)

object ReduceOptions {

  implicit val reduceOptionsSchema: Schema.Block[ReduceOptions] = Schema
    .block[ReduceOptions]("options")
    .addOpt("calculation", _.calculation)
    .addOpt("fields", _.fields)
    .addOpt("limit", _.limit)
    .addOpt("values", _.values)
    .build

}
