package io.gdashboard.terraform

import io.gdashboard.terraform.ast.{Element, Schema}

sealed trait FieldOverride

object FieldOverride {

  final case class ByName(name: Option[String], field: Option[FieldOptions])       extends FieldOverride
  final case class ByRegex(regex: Option[String], field: Option[FieldOptions])     extends FieldOverride
  final case class ByType(tpe: Option[String], field: Option[FieldOptions])        extends FieldOverride
  final case class ByQueryId(queryId: Option[String], field: Option[FieldOptions]) extends FieldOverride
  final case class Unknown(name: Option[String], field: Option[FieldOptions])      extends FieldOverride

  implicit val byNameSchema: Schema.Block[ByName] = Schema
    .block[ByName]("by_name")
    .addOpt("name", _.name)
    .addOpt("field", _.field)
    .build

  implicit val byRegexSchema: Schema.Block[ByRegex] = Schema
    .block[ByRegex]("by_regex")
    .addOpt("regex", _.regex)
    .addOpt("field", _.field)
    .build

  implicit val byTypeSchema: Schema.Block[ByType] = Schema
    .block[ByType]("by_type")
    .addOpt("type", _.tpe)
    .addOpt("field", _.field)
    .build

  implicit val byQueryIdSchema: Schema.Block[ByQueryId] = Schema
    .block[ByQueryId]("by_query_id")
    .addOpt("query_id", _.queryId)
    .addOpt("field", _.field)
    .build

  implicit val unknownSchema: Schema.Block[Unknown] = Schema
    .block[Unknown]("unknown")
    .addOpt("tpe", _.name)
    .addOpt("field", _.field)
    .build

  implicit val fieldOverrideSchema: Schema.Block[FieldOverride] =
    new Schema.Block[FieldOverride] {
      def toElement(field: FieldOverride): Element.Block =
        field match {
          case byName: ByName       => Schema.Block[ByName].toElement(byName)
          case byRegex: ByRegex     => Schema.Block[ByRegex].toElement(byRegex)
          case byType: ByType       => Schema.Block[ByType].toElement(byType)
          case byQueryId: ByQueryId => Schema.Block[ByQueryId].toElement(byQueryId)
          case unknown: Unknown     => Schema.Block[Unknown].toElement(unknown)
        }

      def toElement(name: String, field: FieldOverride): Element =
        field match {
          case byName: ByName       => Schema.Block[ByName].toElement(name, byName)
          case byRegex: ByRegex     => Schema.Block[ByRegex].toElement(name, byRegex)
          case byType: ByType       => Schema.Block[ByType].toElement(name, byType)
          case byQueryId: ByQueryId => Schema.Block[ByQueryId].toElement(name, byQueryId)
          case unknown: Unknown     => Schema.Block[Unknown].toElement(name, unknown)
        }

      def elements(field: FieldOverride): List[Element] =
        field match {
          case byName: ByName       => Schema.Block[ByName].elements(byName)
          case byRegex: ByRegex     => Schema.Block[ByRegex].elements(byRegex)
          case byType: ByType       => Schema.Block[ByType].elements(byType)
          case byQueryId: ByQueryId => Schema.Block[ByQueryId].elements(byQueryId)
          case unknown: Unknown     => Schema.Block[Unknown].elements(unknown)
        }
    }
}
