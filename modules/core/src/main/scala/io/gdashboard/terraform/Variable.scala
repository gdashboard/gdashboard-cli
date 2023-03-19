package io.gdashboard.terraform

import io.gdashboard.terraform.ast.{Element, Schema}

sealed trait Variable extends Product with Serializable

object Variable {

  final case class Const(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      value: Option[String]
  ) extends Variable

  final case class Custom(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      options: List[CustomOption],
      multiValue: Option[Boolean],
      includeAll: Option[IncludeAll]
  ) extends Variable

  final case class Textbox(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      defaultValue: Option[String]
  ) extends Variable

  final case class Adhoc(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      datasource: Option[AdhocDataSource]
  ) extends Variable

  final case class DataSource(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      multiValue: Option[Boolean],
      includeAll: Option[IncludeAll],
      source: Option[DataSourceSelector]
  ) extends Variable

  final case class Query(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      multiValue: Option[Boolean],
      includeAll: Option[IncludeAll],
      refresh: Option[String],
      regex: Option[String],
      sort: Option[QuerySort],
      target: List[QueryTarget]
  ) extends Variable

  final case class Interval(
      name: Option[String],
      label: Option[String],
      description: Option[String],
      hide: Option[String],
      intervals: List[String],
      auto: Option[IntervalAuto]
  ) extends Variable

  final case class CustomOption(
      text: Option[String],
      value: Option[String],
      selected: Option[Boolean]
  )

  final case class IncludeAll(
      enabled: Option[Boolean],
      customValue: Option[String]
  )

  final case class AdhocDataSource(
      tpe: Option[String],
      uid: Option[String]
  )

  final case class DataSourceSelector(
      tpe: Option[String],
      filter: Option[String]
  )

  final case class QuerySort(
      tpe: Option[String],
      order: Option[String]
  )

  sealed trait QueryTarget

  object QueryTarget {

    final case class Prometheus(uid: Option[String], expr: Option[String]) extends QueryTarget

    implicit val prometheusSchema: Schema.Block[Prometheus] = Schema
      .block[Prometheus]("prometheus")
      .addOpt("uid", _.uid)
      .addOpt("expr", _.expr)
      .build

    implicit val queryTargetSchema: Schema.Block[QueryTarget] =
      new Schema.Block[QueryTarget] {
        def toElement(target: QueryTarget): Element =
          target match {
            case prometheus: Prometheus => Schema.Block[Prometheus].toElement("prometheus", prometheus)
          }

        def toElement(name: String, target: QueryTarget): Element =
          target match {
            case prometheus: Prometheus => Schema.Block[Prometheus].toElement(name, prometheus)
          }
      }
  }

  final case class IntervalAuto(
      enabled: Option[Boolean],
      stepCount: Option[Int],
      minInterval: Option[String]
  )

  implicit val customOptionSchema: Schema.Block[CustomOption] = Schema
    .block[CustomOption]("option")
    .addOpt("text", _.text)
    .addOpt("value", _.value)
    .addOpt("selected", _.selected)
    .build

  implicit val includeAllSchema: Schema.Block[IncludeAll] = Schema
    .block[IncludeAll]("include_all")
    .addOpt("enabled", _.enabled)
    .addOpt("custom_value", _.customValue)
    .build

  implicit val adhocDataSourceSchema: Schema.Block[AdhocDataSource] = Schema
    .block[AdhocDataSource]("datasource")
    .addOpt("type", _.tpe)
    .addOpt("uid", _.uid)
    .build

  implicit val dataSourceSelectorSchema: Schema.Block[DataSourceSelector] = Schema
    .block[DataSourceSelector]("source")
    .addOpt("type", _.tpe)
    .addOpt("filter", _.filter)
    .build

  implicit val querySortSchema: Schema.Block[QuerySort] = Schema
    .block[QuerySort]("sort")
    .addOpt("type", _.tpe)
    .addOpt("order", _.order)
    .build

  implicit val intervalAuto: Schema.Block[IntervalAuto] = Schema
    .block[IntervalAuto]("auto")
    .addOpt("enabled", _.enabled)
    .addOpt("step_count", _.stepCount)
    .addOpt("interval", _.minInterval)
    .build

  implicit val constSchema: Schema.Block[Const] = Schema
    .block[Const]("const")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("value", _.value)
    .build

  implicit val customSchema: Schema.Block[Custom] = Schema
    .block[Custom]("custom")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .addOpt("multi", _.multiValue)
    .addOpt("include_all", _.includeAll)
    .add("option", _.options)(Schema.listInlineBlockSchema)
    .build

  implicit val textboxSchema: Schema.Block[Textbox] = Schema
    .block[Textbox]("textbox")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .addOpt("default_value", _.defaultValue)
    .build

  implicit val adhocSchema: Schema.Block[Adhoc] = Schema
    .block[Adhoc]("adhoc")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .addOpt("datasource", _.datasource)
    .build

  implicit val dataSourceSchema: Schema.Block[DataSource] = Schema
    .block[DataSource]("datasource")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .addOpt("multi", _.multiValue)
    .addOpt("include_all", _.includeAll)
    .addOpt("source", _.source)
    .build

  implicit val querySchema: Schema.Block[Query] = Schema
    .block[Query]("query")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .addOpt("multi", _.multiValue)
    .addOpt("include_all", _.includeAll)
    .addOpt("refresh", _.refresh)
    .addOpt("regex", _.regex)
    .add("target", _.target)
    .addOpt("sort", _.sort)
    .build

  implicit val intervalSchema: Schema.Block[Interval] = Schema
    .block[Interval]("interval")
    .addOpt("name", _.name)
    .addOpt("label", _.label)
    .addOpt("description", _.description)
    .addOpt("hide", _.hide)
    .add("intervals", _.intervals)
    .addOpt("auto", _.auto)
    .build

  implicit val variableSchema: Schema.Block[Variable] =
    new Schema.Block[Variable] {
      def toElement(variable: Variable): Element =
        variable match {
          case const: Const           => Schema.Block[Const].toElement(const)
          case custom: Custom         => Schema.Block[Custom].toElement(custom)
          case textbox: Textbox       => Schema.Block[Textbox].toElement(textbox)
          case adhoc: Adhoc           => Schema.Block[Adhoc].toElement(adhoc)
          case dataSource: DataSource => Schema.Block[DataSource].toElement(dataSource)
          case query: Query           => Schema.Block[Query].toElement(query)
          case interval: Interval     => Schema.Block[Interval].toElement(interval)
        }

      def toElement(name: String, variable: Variable): Element =
        variable match {
          case const: Const           => Schema.Block[Const].toElement(name, const)
          case custom: Custom         => Schema.Block[Custom].toElement(name, custom)
          case textbox: Textbox       => Schema.Block[Textbox].toElement(name, textbox)
          case adhoc: Adhoc           => Schema.Block[Adhoc].toElement(name, adhoc)
          case dataSource: DataSource => Schema.Block[DataSource].toElement(name, dataSource)
          case query: Query           => Schema.Block[Query].toElement(name, query)
          case interval: Interval     => Schema.Block[Interval].toElement(name, interval)
        }
    }

}
