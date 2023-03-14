package io.gdashboard.terraform.ast

sealed trait Primitive {
  def asString: String
}

object Primitive {
  trait From[A] {
    def from(a: A): Primitive
  }

  object From {
    def apply[A](implicit ev: From[A]): From[A] = ev

    implicit val fromString: From[String]   = a => Str(a)
    implicit val fromInt: From[Int]         = a => Int32(a)
    implicit val fromDouble: From[Double]   = a => Double64(a)
    implicit val fromBoolean: From[Boolean] = a => Bool(a)
  }

  final case class Str(value: String) extends Primitive {
    def asString: String = quote(value)
  }

  final case class Int32(value: Int) extends Primitive {
    def asString: String = value.toString
  }

  final case class Double64(value: Double) extends Primitive {
    def asString: String = value.toString
  }

  final case class Bool(value: Boolean) extends Primitive {
    def asString: String = value.toString
  }

  final case class Arr(value: List[Primitive]) extends Primitive {
    def asString: String = value.map(_.asString).mkString("[", ",", "]")
  }

  private def quote(string: String): String =
    "\"" + string.replace("\"", "'").replace("${", "$${") + "\""
}
