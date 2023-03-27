package io.gdashboard

import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import io.gdashboard.terraform.ast.{Element, Primitive, Schema}
import io.scalaland.chimney.dsl._

object Generator {
  import Conversions._

  final case class Section(
      title: Option[String],
      collapsible: Boolean,
      collapsed: Boolean,
      inner: List[GeneratedPanel]
  )

  final case class GeneratedPanel(
      panel: grafana.Panel,
      selector: String,
      element: Element
  )

  def generate[F[_]: Concurrent: Console](
      dashboard: grafana.Dashboard,
      groupBySections: Boolean
  ): F[List[TerraformFile]] =
    Counters.make[F].flatMap { implicit counters: Counters[F] =>
      process(dashboard, groupBySections)
    }

  private def process[F[_]: Monad: Console: Counters](
      dash: grafana.Dashboard,
      groupBySections: Boolean
  ): F[List[TerraformFile]] = {
    // val inner = dash.panels.flatMap(_.panels.toList.flatten)
    // val all   = dash.panels ++ inner
    // println(all.groupBy(_.`type`).mapValues(_.length).toMap)

    val groupedF = dash.panels
      .sortBy(panel => panel.gridPos.y.zip(panel.gridPos.x))
      .foldLeftM(List.empty[Section]) {
        case (acc, panel) if panel.`type` == "row" =>
          val title     = panel.title.getOrElse("Row title")
          val collapsed = panel.collapsed.getOrElse(false)

          for {
            children <- panel.panels.getOrElse(Nil).flatTraverse(panel => makePanel(panel).map(_.toList))
          } yield acc :+ Section(Some(title), collapsible = true, collapsed = collapsed, inner = children)

        case (acc, panel) =>
          acc.lastOption match {
            case Some(last) =>
              for {
                p <- makePanel(panel)
              } yield acc.init :+ last.copy(inner = last.inner ++ p)

            case None =>
              for {
                p <- makePanel(panel)
              } yield acc :+ Section(title = None, collapsible = false, collapsed = false, p.toList)
          }
      }

    groupedF.map { grouped =>
      val sections: List[terraform.datasource.Dashboard.Section] = grouped.map { section =>
        terraform.datasource.Dashboard.Section(
          title = section.title,
          collapsed = Option.when(section.collapsible && section.collapsed)(section.collapsed),
          panels = section.inner.map { panel =>
            val size = panel.panel.gridPos.w.zip(panel.panel.gridPos.h) match {
              case Some((width, height)) => terraform.datasource.Dashboard.Size.Ref(height, width, "local.size")
              case None                  => terraform.datasource.Dashboard.Size.Ref(4, 4, "local.size")
            }

            terraform.datasource.Dashboard.Panel(panel.selector, size)
          }
        )
      }

      val locals = {
        val sizes =
          sections.flatMap(_.panels).map(_.size).distinct.sortBy(_.width).collect {
            case ref: terraform.datasource.Dashboard.Size.Ref =>
              Element.Block(
                name = s"${ref.varName} =",
                children = List(
                  Element.Attribute("width", Primitive.Int32(ref.width)),
                  Element.Attribute("height", Primitive.Int32(ref.height))
                )
              )
          }

        Element.Block("locals", List(Element.Block("size =", sizes)))
      }

      val tfDashboard = makeDashboard(dash, sections)

      val name = sanitize(tfDashboard.title)

      val files = if (groupBySections) {
        val (_, files) = grouped.foldLeft((0, List.empty[TerraformFile])) { case ((counter, acc), section) =>
          val content = section.inner.map(_.element.render).mkString("\n\n")
          section.title match {
            case Some(title) =>
              val file = TerraformFile(sanitize(title) + ".tf", content)
              (counter, acc :+ file)

            case None =>
              val id   = counter + 1
              val file = TerraformFile(s"panels-$id.tf", content)

              (id, acc :+ file)

          }
        }

        files
      } else {
        List(
          TerraformFile(
            "panels.tf",
            grouped.flatMap(_.inner).map(_.element.render).mkString("\n\n")
          )
        )
      }

      val dashboardFile = TerraformFile(
        "dashboard.tf",
        locals.render + "\n\n" + Schema.DataSource[terraform.datasource.Dashboard].toElement(name, tfDashboard).render
      )

      files :+ dashboardFile
    }
  }

  private def makeDashboard(
      dash: grafana.Dashboard,
      sections: List[terraform.datasource.Dashboard.Section]
  ): terraform.datasource.Dashboard = {
    def pickerDefined =
      dash.timepicker.exists(v =>
        v.time_options.exists(_.nonEmpty) || v.refresh_intervals.exists(_.nonEmpty) ||
          v.hidden.isDefined || v.now_delay.isDefined
      )

    val tooltip =
      dash.graphTooltip.map(v => if (v == 1) "shared-crosshair" else if (v == 2) "shared-tooltip" else "default")

    val timeOptions =
      Option.when(
        dash.time.exists(v => v.to.isDefined || v.from.isDefined) ||
          pickerDefined ||
          dash.liveNow.contains(true) ||
          dash.timezone.exists(v => v.nonEmpty && v != "browser") ||
          dash.weekStart.exists(_.nonEmpty)
      )(
        terraform.datasource.Dashboard.TimeOptions(
          dash.liveNow.filter(identity),
          dash.timezone.filter(v => v.nonEmpty && v != "browser"),
          weekStart = dash.weekStart.filter(_.nonEmpty),
          defaultRange = dash.time.map(time => terraform.Time(time.from, time.to)),
          picker = Option
            .when(pickerDefined)(
              dash.timepicker.map(picker =>
                terraform.datasource.Dashboard.Picker(
                  hide = picker.hidden,
                  nowDelay = picker.now_delay,
                  refreshIntervals = picker.refresh_intervals.getOrElse(Nil),
                  timeOptions = picker.time_options.getOrElse(Nil)
                )
              )
            )
            .flatten
        )
      )

    val variables =
      dash.templating.map(_.list).toList.flatten.flatMap(template => makeVariable(template))

    terraform.datasource.Dashboard(
      title = dash.title.getOrElse("Dashboard"),
      description = dash.description,
      uid = dash.uid,
      version = dash.version,
      editable = dash.editable,
      style = dash.style,
      graphTooltip = tooltip,
      tags = dash.tags,
      time = timeOptions,
      variables = variables,
      layout = Option.when(sections.nonEmpty)(terraform.datasource.Dashboard.Layout(sections))
    )
  }

  private def makePanel[F[_]: Monad: Console: Counters](panel: grafana.Panel): F[Option[GeneratedPanel]] =
    panel.`type` match {
      case "gauge" =>
        Counters[F].nextGauge.map { counter =>
          val id = panel.title.map(sanitize).getOrElse(s"gauge_$counter")

          Some(
            GeneratedPanel(
              panel,
              s"data.gdashboard_gauge.$id.json",
              Schema.DataSource[terraform.datasource.Gauge].toElement(id, makeGauge(panel))
            )
          )
        }

      case "stat" =>
        Counters[F].nextStat.map { counter =>
          val id = panel.title.map(sanitize).getOrElse(s"stat_$counter")

          Some(
            GeneratedPanel(
              panel,
              s"data.gdashboard_stat.$id.json",
              Schema.DataSource[terraform.datasource.Stat].toElement(id, makeStat(panel))
            )
          )
        }

      case "timeseries" =>
        Counters[F].nextTimeseries.map { counter =>
          val id = panel.title.map(sanitize).getOrElse(s"series_$counter")

          Some(
            GeneratedPanel(
              panel,
              s"data.gdashboard_timeseries.$id.json",
              Schema.DataSource[terraform.datasource.Timeseries].toElement(id, makeTimeseries(panel))
            )
          )
        }

      case "table" =>
        Counters[F].nextTable.map { counter =>
          val id = panel.title.map(sanitize).getOrElse(s"table_$counter")

          Some(
            GeneratedPanel(
              panel,
              s"data.gdashboard_table.$id.json",
              Schema.DataSource[terraform.datasource.Table].toElement(id, makeTable(panel))
            )
          )
        }

      case _ =>
        Console[F].println(s"Unknown panel type [${panel.`type`}]. Title [${panel.title}].").as(None)
    }

  private def makeVariable(template: grafana.Templating): Option[terraform.Variable] = {
    val name        = template.name
    val label       = template.label
    val description = template.description
    val hide        = template.hide.flatMap(v => if (v == 2) Some("variable") else if (v == 1) Some("label") else None)

    def multi = template.multi.filter(identity)

    def includeAll = Option.when(template.includeAll.contains(true) || template.allValue.isDefined)(
      terraform.Variable.IncludeAll(template.includeAll, template.allValue)
    )

    template.`type` match {
      case Some("custom") =>
        Some(
          terraform.Variable.Custom(
            name = name,
            label = label,
            description = description,
            hide = hide,
            options = template.options.map(_.transformInto[terraform.Variable.CustomOption]),
            multiValue = multi,
            includeAll = includeAll
          )
        )

      case Some("const") =>
        Some(terraform.Variable.Const(name, label, description, template.query.flatMap(_.asString)))

      case Some("textbox") =>
        Some(terraform.Variable.Textbox(name, label, description, hide, template.query.flatMap(_.asString)))

      case Some("adhoc") =>
        Some(
          terraform.Variable.Adhoc(
            name = name,
            label = label,
            description = description,
            hide = hide,
            datasource = Option.when(template.datasource.exists(d => d.`type`.isDefined || d.uid.isDefined))(
              terraform.Variable.AdhocDataSource(
                tpe = template.datasource.flatMap(_.`type`),
                uid = template.datasource.flatMap(_.uid)
              )
            )
          )
        )

      case Some("datasource") =>
        Some(
          terraform.Variable.DataSource(
            name = name,
            label = label,
            description = description,
            hide = hide,
            multiValue = multi,
            includeAll = includeAll,
            source = Option.when(template.query.isDefined || template.regex.isDefined)(
              terraform.Variable.DataSourceSelector(
                template.query.flatMap(_.asString).filter(_.nonEmpty),
                template.regex.filter(_.nonEmpty)
              )
            )
          )
        )

      case Some("query") =>
        Some(
          terraform.Variable.Query(
            name = name,
            label = label,
            description = description,
            hide = hide,
            multiValue = multi,
            includeAll = includeAll,
            refresh = template.refresh.flatMap(v =>
              if (v == 1) Some("dashboard-load") else if (v == 2) Some("time-range-change") else None
            ),
            regex = template.regex.filter(_.nonEmpty),
            sort = template.sort.flatMap {
              case 0 => Some(terraform.Variable.QuerySort(Some("disabled"), None))
              case 1 => Some(terraform.Variable.QuerySort(Some("alphabetical"), Some("asc")))
              case 2 => Some(terraform.Variable.QuerySort(Some("alphabetical"), Some("desc")))
              case 3 => Some(terraform.Variable.QuerySort(Some("numerical"), Some("asc")))
              case 4 => Some(terraform.Variable.QuerySort(Some("numerical"), Some("desc")))
              case 5 => Some(terraform.Variable.QuerySort(Some("alphabetical-case-insensitive"), Some("asc")))
              case 6 => Some(terraform.Variable.QuerySort(Some("alphabetical-case-insensitive"), Some("desc")))
              case _ => None
            },
            target = template.datasource.flatMap {
              case grafana.Datasource(Some("prometheus"), uid) =>
                Some(
                  terraform.Variable.QueryTarget.Prometheus(
                    uid = uid,
                    expr = template.query.flatMap(_.hcursor.get[String]("query").toOption)
                  )
                )
              case _ =>
                None
            }.toList
          )
        )

      case Some("interval") =>
        Some(
          terraform.Variable.Interval(
            name = name,
            label = label,
            description = description,
            hide = hide,
            intervals = template.query.flatMap(_.asString).map(_.split(",").toList).getOrElse(Nil),
            auto = Option.when(template.auto.isDefined || template.autoCount.isDefined || template.autoMin.isDefined)(
              terraform.Variable.IntervalAuto(
                enabled = template.auto,
                stepCount = template.autoCount,
                minInterval = template.autoMin
              )
            )
          )
        )

      case _ =>
        None
    }
  }

  private def makeTimeseries(panel: grafana.Panel): terraform.datasource.Timeseries =
    terraform.datasource.Timeseries(
      title = panel.title.getOrElse(""),
      description = panel.description,
      field = panel.fieldConfig.flatMap(_.defaults).map(_.transformInto[terraform.FieldOptions]),
      axis = panel.fieldConfig.flatMap(_.defaults).flatMap(_.custom).map(_.transformInto[terraform.Axis]),
      graph = panel.fieldConfig
        .flatMap(_.defaults)
        .flatMap(_.custom)
        .map(_.transformInto[terraform.datasource.Timeseries.Graph]),
      legend = panel.options.flatMap(_.legend).map(_.transformInto[terraform.Legend]),
      overrides = panel.fieldConfig.flatMap(_.overrides).toList.flatten.map(_.transformInto[terraform.FieldOverride]),
      queries = panel.targets.getOrElse(Nil).map(_.transformInto[terraform.Query]),
      tooltip = panel.options.flatMap(_.tooltip).map(_.transformInto[terraform.Tooltip]),
      transform = Nil
    )

  private def makeGauge(panel: grafana.Panel): terraform.datasource.Gauge = {
    def makeGraph(options: grafana.Options): terraform.datasource.Gauge.Graph =
      options
        .into[terraform.datasource.Gauge.Graph]
        .withFieldConst(_.textSize, options.text.map(ts => terraform.TextSize(ts.titleSize, ts.valueSize)))
        .withFieldConst(
          _.options,
          options.reduceOptions.map(ro => terraform.ReduceOptions(ro.calcs.headOption, ro.fields, ro.limit, ro.values))
        )
        .transform

    terraform.datasource.Gauge(
      title = panel.title.getOrElse(""),
      description = panel.description,
      field = panel.fieldConfig.flatMap(_.defaults).map(_.transformInto[terraform.FieldOptions]),
      graph = panel.options.map(opts => makeGraph(opts)),
      overrides = panel.fieldConfig.flatMap(_.overrides).toList.flatten.map(_.transformInto[terraform.FieldOverride]),
      queries = panel.targets.getOrElse(Nil).map(_.transformInto[terraform.Query]),
      transform = Nil
    )
  }

  private def makeStat(panel: grafana.Panel): terraform.datasource.Stat = {
    def makeGraph(options: grafana.Options): terraform.datasource.Stat.Graph =
      options
        .into[terraform.datasource.Stat.Graph]
        .withFieldRenamed(_.justifyMode, _.textAlignment)
        .withFieldConst(_.textSize, options.text.map(ts => terraform.TextSize(ts.titleSize, ts.valueSize)))
        .withFieldConst(
          _.options,
          options.reduceOptions.map(ro => terraform.ReduceOptions(ro.calcs.headOption, ro.fields, ro.limit, ro.values))
        )
        .transform

    terraform.datasource.Stat(
      title = panel.title.getOrElse(""),
      description = panel.description,
      field = panel.fieldConfig.flatMap(_.defaults).map(_.transformInto[terraform.FieldOptions]),
      graph = panel.options.map(opts => makeGraph(opts)),
      overrides = panel.fieldConfig.flatMap(_.overrides).toList.flatten.map(_.transformInto[terraform.FieldOverride]),
      queries = panel.targets.getOrElse(Nil).map(_.transformInto[terraform.Query]),
      transform = Nil
    )
  }

  private def makeTable(panel: grafana.Panel): terraform.datasource.Table = {
    def makeGraph(
        options: Option[grafana.Options],
        custom: Option[grafana.Custom]
    ): Option[terraform.datasource.Table.Graph] =
      Option.when(options.isDefined || custom.isDefined)(
        terraform.datasource.Table.Graph(
          cell = Option.when(custom.exists(c => c.displayMode.exists(_.nonEmpty) || c.inspect.contains(true)))(
            terraform.datasource.Table.Cell(
              custom.flatMap(_.displayMode),
              custom.flatMap(_.inspect)
            )
          ),
          column = Option.when(
            custom.exists(c =>
              c.align.exists(_.nonEmpty) || c.filterable.contains(true) || c.minWidth.nonEmpty || c.width.nonEmpty
            )
          )(
            terraform.datasource.Table.Column(
              custom.flatMap(_.align),
              custom.flatMap(_.filterable),
              minWidth = custom.flatMap(_.minWidth),
              width = custom.flatMap(_.width)
            )
          ),
          footer = options
            .flatMap(_.footer)
            .map(footer =>
              terraform.datasource.Table.Footer(
                calculations = footer.reducer.getOrElse(Nil),
                fields = footer.fields.toList.flatMap(_.split(",").toList),
                pagination = footer.enablePagination
              )
            ),
          showHeader = options.flatMap(_.showHeader)
        )
      )

    terraform.datasource.Table(
      title = panel.title.getOrElse(""),
      description = panel.description,
      field = panel.fieldConfig.flatMap(_.defaults).map(_.transformInto[terraform.FieldOptions]),
      graph = makeGraph(panel.options, panel.fieldConfig.flatMap(_.defaults).flatMap(_.custom)),
      overrides = panel.fieldConfig.flatMap(_.overrides).toList.flatten.map(_.transformInto[terraform.FieldOverride]),
      queries = panel.targets.getOrElse(Nil).map(_.transformInto[terraform.Query]),
      transform = Nil
    )
  }

  private def sanitize(input: String): String =
    input
      .replace(" ", "_")
      .replace("(", "")
      .replace(")", "")
      .replace("/", "")
      .toLowerCase

}
