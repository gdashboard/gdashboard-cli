package io.gdashboard.grafana

import io.circe.Json

final case class FieldOverride(
    matcher: Option[FieldOverride.Matcher],
    properties: Option[Seq[FieldOverride.Property]]
)

object FieldOverride {
  final case class Matcher(id: Option[String], options: Option[String])

  final case class Property(id: Option[String], value: Json)
}
