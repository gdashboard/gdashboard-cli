package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

final case class Legend(
    calculations: List[String],
    displayMode: Option[String],
    placement: Option[String]
)

object Legend {

  implicit val legendSchema: Schema.Block[Legend] = Schema
    .block[Legend]("legend")
    .add("calculations", _.calculations)
    .addOpt("display_mode", _.displayMode)
    .addOpt("placement", _.placement)
    .build

}