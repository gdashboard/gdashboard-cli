package io.gdashboard.grafana

import io.circe.Json

final case class Color(
    mode: Option[String],
    fixedColor: Option[String],
    seriesBy: Option[String]
)

final case class Current(
    selected: Option[Boolean],
    text: Option[String],
    value: Option[String]
)

case class ReduceOptions(
    values: Option[Boolean],
    fields: Option[String],
    calcs: List[String],
    limit: Option[Int]
)

case class TextSize(
    titleSize: Option[Int],
    valueSize: Option[Int]
)

final case class Custom(
    axisCenteredZero: Option[Boolean],
    axisColorMode: Option[String],
    axisLabel: Option[String],
    axisPlacement: Option[String],
    axisSoftMin: Option[Int],
    axisSoftMax: Option[Int],
    barAlignment: Option[Int],
    drawStyle: Option[String],
    fillOpacity: Option[Int],
    gradientMode: Option[String],
    hideFrom: Option[HideFrom],
    lineInterpolation: Option[String],
    lineStyle: Option[LineStyle],
    lineWidth: Option[Int],
    pointSize: Option[Int],
    scaleDistribution: Option[ScaleDistribution],
    showPoints: Option[String],
    spanNulls: Option[Boolean],
    stacking: Option[Stacking],
    thresholdsStyle: Option[Color],
    // table
    align: Option[String],
    displayMode: Option[String],
    inspect: Option[Boolean],
    filterable: Option[Boolean],
    minWidth: Option[Int],
    width: Option[Int]
)

final case class FieldOptions(
    color: Option[Color],
    custom: Option[Custom],
    links: Option[List[Link]],
    mappings: Option[List[Mapping]],
    max: Option[Int],
    min: Option[Int],
    decimals: Option[Int],
    noValue: Option[Int],
    thresholds: Option[Thresholds],
    unit: Option[String]
)

final case class FieldConfig(
    defaults: Option[FieldOptions],
    overrides: Option[List[FieldOverride]]
)

final case class GridPos(
    h: Option[Int],
    w: Option[Int],
    x: Option[Int],
    y: Option[Int]
)

final case class HideFrom(
    legend: Option[Boolean],
    tooltip: Option[Boolean],
    viz: Option[Boolean]
)

final case class Inputs(
    name: Option[String],
    label: Option[String],
    description: Option[String],
    `type`: Option[String],
    pluginId: Option[String],
    pluginName: Option[String]
)

final case class Legend(
    calcs: Option[List[String]],
    displayMode: Option[String],
    placement: Option[String],
    showLegend: Option[Boolean]
)

final case class LineStyle(
    fill: Option[String]
)

final case class Link(
    icon: Option[String],
    tags: Option[List[String]],
    targetBlank: Option[Boolean],
    title: Option[String],
    `type`: Option[String],
    url: Option[String]
)

final case class TemplatesList(
    list: List[Templating]
)

final case class Templating(
    current: Option[Current],
    hide: Option[Int],
    includeAll: Option[Boolean],
    label: Option[String],
    description: Option[String],
    multi: Option[Boolean],
    name: Option[String],
    options: List[Current],
    query: Option[Json],
    refresh: Option[Int],
    regex: Option[String],
    skipUrlSync: Option[Boolean],
    `type`: Option[String],
    datasource: Option[Datasource],
    definition: Option[String],
    sort: Option[Int],
    allValue: Option[String],
    tagValuesQuery: Option[String],
    tagsQuery: Option[String],
    useTags: Option[Boolean],
    auto: Option[Boolean],
    autoCount: Option[Int],
    autoMin: Option[String]
)

final case class Options(
    legend: Option[Legend],
    tooltip: Option[Tooltip],
    orientation: Option[String],
    textMode: Option[String],
    colorMode: Option[String],
    graphMode: Option[String],
    justifyMode: Option[String],
    displayMode: Option[String],
    showThresholdLabels: Option[Boolean],
    showThresholdMarkers: Option[Boolean],
    content: Option[String],
    mode: Option[String],
    text: Option[TextSize],
    reduceOptions: Option[ReduceOptions],
    // table
    showHeader: Option[Boolean],
    footer: Option[FooterOptions]
)

final case class FooterOptions(
    enablePagination: Option[Boolean],
    fields: Option[String],
    reducer: Option[List[String]],
    show: Option[Boolean]
)

final case class Panel(
    collapsed: Option[Boolean],
    datasource: Datasource,
    gridPos: GridPos,
    id: Option[Int],
    panels: Option[List[Panel]],
    targets: Option[List[Target]],
    title: Option[String],
    `type`: String,
    description: Option[String],
    fieldConfig: Option[FieldConfig],
    links: Option[List[String]],
    options: Option[Options],
    pluginVersion: Option[String],
    hideTimeOverride: Option[Boolean],
    maxDataPoints: Option[Int]
)

final case class Requires(
    `type`: String,
    id: String,
    name: String,
    version: String
)

final case class Dashboard(
    __inputs: Option[List[Inputs]],
    // __elements: Any,
    // __requires: List[Requires],
    // annotations: Annotations,
    editable: Option[Boolean],
    // gnetId: Int,
    graphTooltip: Option[Int],
    id: Option[String],
    links: List[Link],
    liveNow: Option[Boolean],
    panels: List[Panel],
    refresh: Option[Boolean],
    style: Option[String],
    tags: List[String],
    templating: Option[TemplatesList],
    time: Option[Time],
    timepicker: Option[Timepicker],
    timezone: Option[String],
    title: Option[String],
    description: Option[String],
    uid: Option[String],
    version: Option[Int],
    weekStart: Option[String]
    // fiscalYearStartMonth: Int,
)

final case class ScaleDistribution(
    `type`: Option[String],
    log: Option[Int]
)

final case class Stacking(
    group: Option[String],
    mode: Option[String]
)

final case class Steps(
    color: Option[String],
    value: Option[Int]
)

final case class Datasource(`type`: Option[String], uid: Option[String])

final case class Target(
    datasource: Option[Datasource],
    refId: Option[String],
    expr: Option[String],
    format: Option[String],
    hide: Option[Boolean],
    instant: Option[Boolean],
    interval: Option[String],
    intervalFactor: Option[Int],
    legendFormat: Option[String],
    step: Option[Int]
)

final case class Thresholds(
    mode: Option[String],
    steps: Option[List[Steps]]
)

final case class Time(
    from: Option[String],
    to: Option[String]
)

final case class Timepicker(
    hidden: Option[Boolean],
    now_delay: Option[String],
    refresh_intervals: Option[List[String]],
    time_options: Option[List[String]]
)

final case class Tooltip(
    mode: Option[String],
    sort: Option[String]
)
