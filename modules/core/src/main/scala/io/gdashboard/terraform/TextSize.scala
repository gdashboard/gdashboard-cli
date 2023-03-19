package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class TextSize(
    title: Option[Int],
    value: Option[Int]
)

object TextSize {

  implicit val textSizeSchema: Schema.Block[TextSize] = Schema
    .block[TextSize]("text_size")
    .addOpt("title", _.title)
    .addOpt("value", _.value)
    .build

}
