import cats.effect.{ExitCode, IO}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp

object Main extends CommandIOApp(
  name = "gdashboard",
  header = "gdashboard - generate terraform files from Grafana dashboard"
) {

  def main: Opts[IO[ExitCode]] =
    Opts.subcommand("generate", "Generate terraform files from the json file")(
      Opts(IO.realTimeInstant.flatMap(now => IO.println(s"Now is $now").as(ExitCode.Success)))
    )
}

