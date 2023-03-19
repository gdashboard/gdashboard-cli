package io.gdashboard.grafana

import io.circe.{Decoder, DecodingFailure}
import io.circe.generic.auto._

sealed trait Mapping

object Mapping {

  final case class Range(from: Option[Double], to: Option[Double], displayText: Option[String], color: Option[String])
      extends Mapping
  final case class Regex(pattern: Option[String], displayText: Option[String], color: Option[String])   extends Mapping
  final case class Special(`match`: Option[String], displayText: Option[String], color: Option[String]) extends Mapping
  final case class Value(value: Option[String], displayText: Option[String], color: Option[String])     extends Mapping

  implicit val mappingDecoder: Decoder[Mapping] =
    Decoder.instance { cursor =>
      cursor.get[String]("type").flatMap {
        case "range"   => cursor.get[Range]("options")
        case "regex"   => cursor.get[Regex]("options")
        case "special" => cursor.get[Special]("options")
        case "value"   => cursor.get[Value]("options")
        case other =>
          Left(DecodingFailure(s"Cannot decode [$other] as mapping type", cursor.history))
      }
    }

}
