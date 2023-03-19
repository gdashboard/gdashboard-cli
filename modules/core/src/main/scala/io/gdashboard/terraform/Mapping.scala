package io.gdashboard.terraform

import io.gdashboard.terraform.ast.{Element, Schema}

sealed trait Mapping

object Mapping {

  final case class Range(
      from: Option[Double],
      to: Option[Double],
      displayText: Option[String],
      color: Option[String]
  ) extends Mapping

  final case class Regex(
      pattern: Option[String],
      displayText: Option[String],
      color: Option[String]
  ) extends Mapping

  final case class Special(
      matcher: Option[String],
      displayText: Option[String],
      color: Option[String]
  ) extends Mapping

  final case class Value(
      value: Option[String],
      displayText: Option[String],
      color: Option[String]
  ) extends Mapping

  implicit val rangeSchema: Schema.Block[Range] = Schema
    .block[Range]("range")
    .addOpt("from", _.from)
    .addOpt("to", _.to)
    .addOpt("display_text", _.displayText)
    .addOpt("color", _.color)
    .build

  implicit val regexSchema: Schema.Block[Regex] = Schema
    .block[Regex]("regex")
    .addOpt("pattern", _.pattern)
    .addOpt("display_text", _.displayText)
    .addOpt("color", _.color)
    .build

  implicit val specialSchema: Schema.Block[Special] = Schema
    .block[Special]("special")
    .addOpt("match", _.matcher)
    .addOpt("display_text", _.displayText)
    .addOpt("color", _.color)
    .build

  implicit val valueSchema: Schema.Block[Value] = Schema
    .block[Value]("value")
    .addOpt("value", _.value)
    .addOpt("display_text", _.displayText)
    .addOpt("color", _.color)
    .build

  implicit val mappingSchema: Schema.Block[Mapping] =
    new Schema.Block[Mapping] {
      def toElement(mapping: Mapping): Element =
        mapping match {
          case range: Range     => Schema.Block[Range].toElement(range)
          case regex: Regex     => Schema.Block[Regex].toElement(regex)
          case special: Special => Schema.Block[Special].toElement(special)
          case value: Value     => Schema.Block[Value].toElement(value)
        }

      def toElement(name: String, mapping: Mapping): Element =
        mapping match {
          case range: Range     => Schema.Block[Range].toElement(name, range)
          case regex: Regex     => Schema.Block[Regex].toElement(name, regex)
          case special: Special => Schema.Block[Special].toElement(name, special)
          case value: Value     => Schema.Block[Value].toElement(name, value)
        }
    }

}
