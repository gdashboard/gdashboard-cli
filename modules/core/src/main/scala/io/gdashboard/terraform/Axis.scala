package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Axis(
    label: Option[String],
    placement: Option[String],
    softMin: Option[Int],
    softMax: Option[Int],
    scale: Option[Axis.Scale]
)

object Axis {

  final case class Scale(tpe: Option[String], log: Option[Int])

  object Scale {
    implicit val scaleSchema: Schema.Block[Scale] = Schema
      .block[Scale]("scale")
      .addOpt("type", _.tpe)
      .addOpt("log", _.log)
      .build
  }

  implicit val axisSchema: Schema.Block[Axis] = Schema
    .block[Axis]("axis")
    .addOpt("label", _.label)
    .addOpt("placement", _.placement)
    .addOpt("soft_min", _.softMin)
    .addOpt("soft_max", _.softMax)
    .addOpt("scale", _.scale)
    .build

}
