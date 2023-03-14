package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Tooltip(mode: Option[String])

object Tooltip {

  implicit val tooltipSchema: Schema[Tooltip] = Schema
    .block[Tooltip]("tooltip")
    .addOpt("mode", _.mode)
    .build

}
