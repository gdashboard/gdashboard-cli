package io.gdashboard

import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl._

object Conversions {

  implicit val thresholdsTransformer: Transformer[grafana.Thresholds, terraform.Thresholds] =
    Transformer
      .define[grafana.Thresholds, terraform.Thresholds]
      .withFieldComputed(_.steps, _.steps.toList.flatten.map(_.transformInto[terraform.Thresholds.Step]))
      .buildTransformer

  implicit val fieldOptionsTransformer: Transformer[grafana.FieldOptions, terraform.FieldOptions] =
    Transformer
      .define[grafana.FieldOptions, terraform.FieldOptions]
      .withFieldComputed(_.mappings, _.mappings.toList.flatten.map(_.transformInto[terraform.Mapping]))
      .buildTransformer

  implicit val axisTransformer: Transformer[grafana.Custom, terraform.Axis] =
    Transformer
      .define[grafana.Custom, terraform.Axis]
      .withFieldRenamed(_.axisLabel, _.label)
      .withFieldRenamed(_.axisPlacement, _.placement)
      .withFieldRenamed(_.axisSoftMin, _.softMin)
      .withFieldRenamed(_.axisSoftMax, _.softMax)
      .withFieldComputed(_.scale, _.scaleDistribution.map(s => terraform.Axis.Scale(s.`type`, s.log)))
      .buildTransformer

  implicit val graphTransformer: Transformer[grafana.Custom, terraform.datasource.Timeseries.Graph] =
    Transformer
      .define[grafana.Custom, terraform.datasource.Timeseries.Graph]
      .withFieldComputed(_.lineStyle, _.lineStyle.flatMap(_.fill))
      .withFieldComputed(_.spanStackSeries, _.stacking.flatMap(_.mode))
      .buildTransformer

  implicit val legendTransformer: Transformer[grafana.Legend, terraform.Legend] =
    Transformer
      .define[grafana.Legend, terraform.Legend]
      .withFieldComputed(_.calculations, _.calcs.toList.flatten)
      .buildTransformer

  implicit val queryTransformer: Transformer[grafana.Target, terraform.Query] = {
    val prometheusTransformer: Transformer[grafana.Target, terraform.Query.Prometheus] =
      Transformer
        .define[grafana.Target, terraform.Query.Prometheus]
        .withFieldComputed(_.uid, _.datasource.flatMap(_.uid))
        .withFieldRenamed(_.interval, _.minInterval)
        .buildTransformer

    val unknownTransformer: Transformer[grafana.Target, terraform.Query.Unknown] =
      Transformer
        .define[grafana.Target, terraform.Query.Unknown]
        .withFieldComputed(_.uid, _.datasource.flatMap(_.uid))
        .withFieldComputed(_.tpe, _.datasource.flatMap(_.`type`).getOrElse("<empty type>"))
        .buildTransformer

    new Transformer[grafana.Target, terraform.Query] {
      def transform(src: grafana.Target): terraform.Query =
        src.datasource.flatMap(_.`type`) match {
          case Some("prometheus") => prometheusTransformer.transform(src)
          case _                  => unknownTransformer.transform(src)
        }
    }
  }

  implicit val mappingSpecialTransformer: Transformer[grafana.Mapping.Special, terraform.Mapping.Special] =
    Transformer
      .define[grafana.Mapping.Special, terraform.Mapping.Special]
      .withFieldRenamed(_.`match`, _.matcher)
      .buildTransformer

  implicit val fieldOverrideTransformer: Transformer[grafana.FieldOverride, terraform.FieldOverride] = {
    import io.circe.generic.auto._

    def transformField(properties: List[grafana.FieldOverride.Property]): terraform.FieldOptions =
      properties.foldLeft(terraform.FieldOptions(None, None, None, None, None, None, None, Nil)) {
        case (opts, grafana.FieldOverride.Property(Some("unit"), json)) =>
          opts.copy(unit = json.as[String].toOption)

        case (opts, grafana.FieldOverride.Property(Some("decimals"), json)) =>
          opts.copy(decimals = json.as[Int].toOption)

        case (opts, grafana.FieldOverride.Property(Some("min"), json)) =>
          opts.copy(min = json.as[Int].toOption)

        case (opts, grafana.FieldOverride.Property(Some("max"), json)) =>
          opts.copy(max = json.as[Int].toOption)

        case (opts, grafana.FieldOverride.Property(Some("noValue"), json)) =>
          opts.copy(noValue = json.as[Int].toOption)

        case (opts, grafana.FieldOverride.Property(Some("color"), json)) =>
          opts.copy(color = json.as[grafana.Color].toOption.map(_.transformInto[terraform.Color]))

        case (opts, grafana.FieldOverride.Property(Some("mappings"), json)) =>
          opts.copy(mappings =
            json.as[List[grafana.Mapping]].toOption.toList.flatMap(_.map(_.transformInto[terraform.Mapping]))
          )

        case (opts, grafana.FieldOverride.Property(Some("thresholds"), json)) =>
          opts.copy(thresholds = json.as[grafana.Thresholds].toOption.map(_.transformInto[terraform.Thresholds]))

        case (opts, _) =>
          opts
      }

    new Transformer[grafana.FieldOverride, terraform.FieldOverride] {
      def transform(src: grafana.FieldOverride): terraform.FieldOverride = {
        val expected = src.matcher.flatMap(_.options)
        val field    = src.properties.map(props => transformField(props))

        src.matcher.flatMap(_.id) match {
          case Some("byName")       => terraform.FieldOverride.ByName(expected, field)
          case Some("byRegexp")     => terraform.FieldOverride.ByRegex(expected, field)
          case Some("byType")       => terraform.FieldOverride.ByType(expected, field)
          case Some("byFrameRefID") => terraform.FieldOverride.ByQueryId(expected, field)
          case other                => terraform.FieldOverride.Unknown(Some(s"$other - $field - unsupported"), field)
        }
      }
    }
  }

}
