package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Color(
    mode: Option[String],
    fixedColor: Option[String],
    seriesBy: Option[String]
)

object Color {

  implicit val colorSchema: Schema.Block[Color] = Schema
    .block[Color]("color")
    .addOpt("mode", _.mode)
    .addOpt("fixed_color", _.fixedColor)
    .addOpt("series_by", _.seriesBy)
    .build

}
