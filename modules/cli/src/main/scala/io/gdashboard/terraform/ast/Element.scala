package io.gdashboard.terraform.ast

sealed trait Element {
  def nonEmpty: Boolean

  final def render: String = Element.render(this, Nil, 0).mkString("\n")
}

object Element {
  final case class Attribute(name: String, value: Primitive) extends Element {
    def nonEmpty: Boolean = true
  }

  final case class Block(name: String, children: List[Element]) extends Element {
    lazy val nonEmpty: Boolean = children.exists(_.nonEmpty)
  }

  final case class InlineBlock(children: List[Element]) extends Element {
    lazy val nonEmpty: Boolean = children.exists(_.nonEmpty)
  }

  final case class DataSource(tpe: String, name: String, children: List[Element]) extends Element {
    lazy val nonEmpty: Boolean = children.exists(_.nonEmpty)
  }

  def render(element: Element, builder: List[String], offset: Int): List[String] = {
    val prefix = " " * offset

    element match {
      case Element.Attribute(name, value) =>
        builder.appended(s"$prefix$name = ${value.asString}")

      case Element.DataSource(tpe, name, children) =>
        val next   = builder.appended(s"""data "gdashboard_$tpe" "$name" {""")
        val result = children.foldLeft(next)((builder, element) => render(element, builder, offset + 2))
        result.appended("}")

      case Element.InlineBlock(children) if children.nonEmpty =>
        children.foldLeft(builder)((builder, element) => render(element, builder, offset))

      case Element.InlineBlock(children) =>
        builder

      case Element.Block(name, children) if children.nonEmpty =>
        val next   = builder.appended(s"$prefix$name {")
        val result = children.foldLeft(next)((builder, element) => render(element, builder, offset + 2))
        result.appended(s"$prefix}")

      case Element.Block(_, _) =>
        builder
    }
  }
}
