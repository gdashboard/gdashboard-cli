package io.gdashboard.terraform

import io.gdashboard.terraform.ast.{Element, Schema}

sealed trait Query

object Query {

  final case class Prometheus(
      uid: Option[String],
      refId: Option[String],
      expr: Option[String],
      instant: Option[Boolean],
      minInterval: Option[String],
      legendFormat: Option[String]
  ) extends Query

  // final case class CloudWatch() extends Query

  final case class Unknown(
      tpe: String, // type of the target
      uid: Option[String],
      refId: Option[String],
      expr: Option[String],
      instant: Option[Boolean]
  ) extends Query

  implicit val prometheusSchema: Schema.Block[Prometheus] = Schema
    .block[Prometheus]("prometheus")
    .addOpt("uid", _.uid)
    .addOpt("ref_id", _.refId)
    .addOpt("expr", _.expr)
    .addOpt("instant", _.instant)
    .addOpt("min_interval", _.minInterval)
    .addOpt("legend_format", _.legendFormat)
    .build

  implicit val unknownSchema: Schema.Block[Unknown] = Schema
    .block[Unknown]("unknown")
    .addOpt("uid", _.uid)
    .addOpt("ref_id", _.refId)
    .addOpt("expr", _.expr)
    .addOpt("instant", _.instant)
    .build

  implicit val querySchema: Schema.Block[Query] =
    new Schema.Block[Query] {
      def toElement(query: Query): Element =
        query match {
          case prometheus: Prometheus => Schema.Block[Prometheus].toElement(prometheus)
          case unknown: Unknown       => Schema.Block[Unknown].toElement(unknown)
        }

      def toElement(name: String, query: Query): Element =
        query match {
          case prometheus: Prometheus => Schema[Prometheus].toElement(name, prometheus)
          case unknown: Unknown       => Schema[Unknown].toElement(s"$name - ${unknown.tpe}", unknown)
        }
    }

}
