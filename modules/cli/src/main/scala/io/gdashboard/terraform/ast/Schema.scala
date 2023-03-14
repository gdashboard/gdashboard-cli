package io.gdashboard.terraform.ast

trait Schema[A] {
  def toElement(name: String, a: A): Element
}

object Schema {

  def apply[A](implicit ev: Schema[A]): Schema[A] = ev

  trait Block[A] extends Schema[A] {
    def toElement(a: A): Element
  }

  object Block {
    def apply[A](implicit ev: Block[A]): Block[A] = ev
  }

  trait DataSource[A] extends Schema[A] {
    override def toElement(name: String, a: A): Element.DataSource
  }

  object DataSource {
    def apply[A](implicit ev: DataSource[A]): DataSource[A] = ev
  }

  sealed abstract class BlockSchemaBuilder[A](blockName: String, fields: List[A => List[Element]]) {
    def add[B: Schema](name: String, focus: A => B): BlockSchemaBuilder[A] =
      new BlockSchemaBuilder[A](
        blockName,
        fields :+ ((a: A) => List(Schema[B].toElement(name, focus(a))))
      ) {}

    def add[B: Schema.Block](focus: A => B): BlockSchemaBuilder[A] =
      new BlockSchemaBuilder[A](
        blockName,
        fields :+ ((a: A) => List(Schema.Block[B].toElement(focus(a))))
      ) {}

    def addOpt[B: Schema](name: String, focus: A => Option[B]): BlockSchemaBuilder[A] =
      new BlockSchemaBuilder[A](
        blockName,
        fields :+ ((a: A) => focus(a).map(b => Schema[B].toElement(name, b)).toList)
      ) {}

    def build: Schema.Block[A] =
      new Schema.Block[A] {
        def toElement(a: A): Element.Block =
          toElement(blockName, a)

        def toElement(name: String, a: A): Element.Block =
          Element.Block(name, fields.flatMap(_.apply(a)))
      }
  }

  sealed abstract class DataSourceSchemaBuilder[A](sourceName: String, fields: List[A => List[Element]]) {
    def add[B: Schema](name: String, focus: A => B): DataSourceSchemaBuilder[A] =
      new DataSourceSchemaBuilder[A](
        sourceName,
        fields :+ ((a: A) => List(Schema[B].toElement(name, focus(a))))
      ) {}

    def addOpt[B: Schema](name: String, focus: A => Option[B]): DataSourceSchemaBuilder[A] =
      new DataSourceSchemaBuilder[A](
        sourceName,
        fields :+ ((a: A) => focus(a).map(b => Schema[B].toElement(name, b)).toList)
      ) {}

    def build: Schema.DataSource[A] =
      new Schema.DataSource[A] {
        def toElement(name: String, a: A): Element.DataSource =
          Element.DataSource(sourceName, name, fields.flatMap(_.apply(a)))
      }
  }

  def block[A](name: String): BlockSchemaBuilder[A] =
    new BlockSchemaBuilder[A](name, Nil) {}

  def dataSource[A](tpe: String): DataSourceSchemaBuilder[A] =
    new DataSourceSchemaBuilder[A](tpe, Nil) {}

  implicit val stringSchema: Schema[String] =
    (name, str) => Element.Attribute(name, Primitive.Str(str))

  implicit val intSchema: Schema[Int] =
    (name, int) => Element.Attribute(name, Primitive.Int32(int))

  implicit val doubleSchema: Schema[Double] =
    (name, int) => Element.Attribute(name, Primitive.Double64(int))

  implicit val booleanSchema: Schema[Boolean] =
    (name, bool) => Element.Attribute(name, Primitive.Bool(bool))

  implicit def primitiveListSchema[A: Primitive.From]: Schema[List[A]] =
    (name, list) => Element.Attribute(name, Primitive.Arr(list.map(Primitive.From[A].from)))

  implicit def listBlockSchema[A: Schema.Block]: Schema[List[A]] =
    (name, list) => Element.Block(name, list.map(Schema.Block[A].toElement))

  def listInlineBlockSchema[A: Schema.Block]: Schema[List[A]] =
    (_, list) => Element.InlineBlock(list.map(Schema.Block[A].toElement))

  val rawStringSchema: Schema[String] =
    (name, str) => Element.Attribute(name, Primitive.RawStr(str))
}
