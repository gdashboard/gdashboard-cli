package io.gdashboard

import cats.data.{NonEmptyList, Validated}
import cats.effect.{ExitCode, IO}
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.reducible._
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import fs2.Stream
import fs2.io.file.{Files, Path}
import io.circe.Json
import io.circe.generic.auto._
import io.gdashboard.Choice.Source
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.ember.client.EmberClientBuilder

object Cli extends CommandIOApp(
      name = "gdashboard-cli",
      header = "Generate Terraform files from Grafana JSON dashboard",
      version = BuildInfo.version
    ) {

  def main: Opts[IO[ExitCode]] =
    opts.map(choice => execute(choice))

  private def execute(choice: Choice): IO[ExitCode] =
    choice match {
      case Choice.Generate(source, groupBySections, output) => generate(source, groupBySections, output)
    }

  private def generate(source: Choice.Source, groupBySections: Boolean, outputDirectory: Path): IO[ExitCode] = {
    def process(content: Json): IO[ExitCode] =
      content.asAccumulating[grafana.Dashboard].toEither match {
        case Right(dashboard) =>
          for {
            files <- Generator.generate[IO](dashboard, groupBySections)
            _     <- Files[IO].createDirectories(outputDirectory)
            _     <- IO.println(s"Writing Terraform files to [$outputDirectory]")
            _ <- files.traverse_ { file =>
              val output = outputDirectory / file.name
              Stream(file.content).through(fs2.text.utf8.encode).through(Files[IO].writeAll(output)).compile.drain
            }
          } yield ExitCode.Success

        case Left(errors) =>
          for {
            _ <- IO.println("Cannot decode input JSON due to: ")
            _ <- errors.traverse(error => IO.println(error.toString()))
          } yield ExitCode.Error
      }

    def download(id: String): IO[Json] =
      EmberClientBuilder.default[IO].build.use { client =>
        for {
          revisions <- client.expect[Revisions](s"https://grafana.com/api/dashboards/$id/revisions")
          revision  <- IO.pure(revisions.items.map(_.revision).maxOption.getOrElse(1))
          _         <- IO.println(s"Downloading dashboard [$id] with revision [$revision]")
          content   <- client.expect[Json](s"https://grafana.com/api/dashboards/$id/revisions/$revision/download")
        } yield content
      }

    source match {
      case Source.File(path) =>
        for {
          text    <- Files[IO].readUtf8(path).compile.string
          content <- IO.fromEither(io.circe.parser.parse(text))
          result  <- process(content)
        } yield result

      case Source.DashboardId(id) =>
        for {
          content <- download(id)
          result  <- process(content)
        } yield result
    }
  }

  final case class Revision(id: Int, revision: Int)

  final case class Revisions(items: Seq[Revision])

  private implicit val revisionsDecoder: EntityDecoder[IO, Revisions] = jsonOf[IO, Revisions]

  private val opts: Opts[Choice] = {
    val dashboardId = Opts
      .option[String]("dashboard-id", "An ID of the Grafana dashboard (https://grafana.com/grafana/dashboards)")
      .orNone

    val groupByRows = Opts.flag("group-by-sections", "Whether to group files by dashboard sections or not").orFalse
    val input       = Opts.option[String]("input", "A path to the file with the JSON of a Grafana dashboard").orNone
    val output      = Opts.argument[String]("The path to an output directory")

    val generate = (input, dashboardId, groupByRows, output).tupled.mapValidated {
      case (None, None, _, _) =>
        Validated.invalidNel("Either `--input` or `--dashboard-id` must be defined")

      case (Some(_), Some(_), _, _) =>
        Validated.invalidNel("Either `--input` or `--dashboard-id` must be defined, not both")

      case (Some(input), _, groupByRows, output) =>
        Validated.valid(Choice.Generate(Choice.Source.File(Path(input)), groupByRows, Path(output)))

      case (_, Some(id), groupByRows, output) =>
        Validated.valid(Choice.Generate(Choice.Source.DashboardId(id), groupByRows, Path(output)))
    }

    NonEmptyList
      .of[Opts[Choice]](
        Opts.subcommand("generate", "Generate Terraform files from a Grafana dashboard")(
          generate
        )
      )
      .reduceK
  }
}

sealed trait Choice
object Choice {
  final case class Generate(
      source: Source,
      groupBySections: Boolean,
      output: Path
  ) extends Choice

  sealed trait Source
  object Source {
    final case class File(path: Path)        extends Source
    final case class DashboardId(id: String) extends Source
  }
}
