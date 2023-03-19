package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Thresholds(
    mode: Option[String],
    steps: List[Thresholds.Step]
)

object Thresholds {

  final case class Step(color: Option[String], value: Option[Int])

  object Step {
    implicit val stepSchema: Schema.Block[Step] = Schema
      .block[Step]("step")
      .addOpt("color", _.color)
      .addOpt("value", _.value)
      .build
  }

  implicit val thresholdSchema: Schema[Thresholds] = Schema
    .block[Thresholds]("thresholds")
    .addOpt("mode", _.mode)
    .add("step", _.steps)(Schema.listInlineBlockSchema)
    .build

}
