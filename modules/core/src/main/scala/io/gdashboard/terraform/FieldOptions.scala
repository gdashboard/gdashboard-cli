package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class FieldOptions(
    unit: Option[String],
    min: Option[Int],
    max: Option[Int],
    decimals: Option[Int],
    noValue: Option[Int],
    color: Option[Color],
    thresholds: Option[Thresholds],
    mappings: List[Mapping]
)

object FieldOptions {

  implicit val fieldOptionsSchema: Schema.Block[FieldOptions] = Schema
    .block[FieldOptions]("field")
    .addOpt("unit", _.unit)
    .addOpt("min", _.min)
    .addOpt("max", _.max)
    .addOpt("decimals", _.decimals)
    .addOpt("no_value", _.noValue)
    .addOpt("color", _.color)
    .addOpt("thresholds", _.thresholds)
    .add("mappings", _.mappings)
    .build

}
