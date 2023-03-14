package io.gdashboard.terraform

import io.gdashboard.terraform.ast.Schema

sealed trait Transform

object Transform {

  implicit val transformSchema: Schema.Block[Transform] = Schema
    .block[Transform]("transform")
    .build

}
