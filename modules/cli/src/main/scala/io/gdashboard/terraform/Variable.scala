package io.gdashboard.terraform

import io.gdashboard.terraform.ast.{Element, Schema}

sealed trait Variable extends Product with Serializable

object Variable {

  final case class Const(
      name: Option[String],
      hide: Option[String],
      value: Option[String]
  ) extends Variable

  final case class Custom(
      name: Option[String],
      hide: Option[String],
      options: List[CustomOption]
  ) extends Variable

  final case class CustomOption(
      text: Option[String],
      value: Option[String],
      selected: Option[Boolean]
  )

  implicit val customOptionSchema: Schema.Block[CustomOption] = Schema
    .block[CustomOption]("option")
    .addOpt("text", _.text)
    .addOpt("value", _.value)
    .addOpt("selected", _.selected)
    .build

  implicit val constSchema: Schema.Block[Const] = Schema
    .block[Const]("const")
    .addOpt("name", _.name)
    .addOpt("hide", _.hide)
    .addOpt("value", _.value)
    .build

  implicit val customSchema: Schema.Block[Custom] = Schema
    .block[Custom]("custom")
    .addOpt("name", _.name)
    .addOpt("hide", _.hide)
    .add("option", _.options)(Schema.listInlineBlockSchema)
    .build

  implicit val variableSchema: Schema.Block[Variable] =
    new Schema.Block[Variable] {
      def toElement(variable: Variable): Element =
        variable match {
          case const: Const   => Schema.Block[Const].toElement(const)
          case custom: Custom => Schema.Block[Custom].toElement(custom)
        }

      def toElement(name: String, variable: Variable): Element =
        variable match {
          case const: Const   => Schema.Block[Const].toElement(name, const)
          case custom: Custom => Schema.Block[Custom].toElement(name, custom)
        }
    }

}
