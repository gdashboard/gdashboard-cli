package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Time(
    from: Option[String],
    to: Option[String]
)

object Time {

  implicit val textSizeSchema: Schema.Block[Time] = Schema
    .block[Time]("time")
    .addOpt("from", _.from)
    .addOpt("to", _.to)
    .build

}
