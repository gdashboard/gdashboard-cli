ThisBuild / scalaVersion := "2.13.10"

ThisBuild / semanticdbEnabled          := true
ThisBuild / semanticdbVersion          := scalafixSemanticdb.revision
ThisBuild / scalafixScalaBinaryVersion := "2.13"

inThisBuild(
  Seq(
    organization  := "io.github.irevive",
    homepage      := Some(url("https://github.com/iRevive/gdashboard-cli")),
    developers    := List(Developer("iRevive", "Maksym Ochenashko", "", url("https://github.com/iRevive"))),
    versionScheme := Some("semver-spec")
  )
)

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports"               % "0.6.0",
  "org.typelevel"        %% "typelevel-scalafix-cats"        % "0.1.5",
  "org.typelevel"        %% "typelevel-scalafix-cats-effect" % "0.1.5",
  "org.typelevel"        %% "typelevel-scalafix-fs2"         % "0.1.5"
)

lazy val binariesMatrix = Map(
  "ubuntu-latest" -> "gdashboard-cli-linux-x84_64",
  "macos-latest"  -> "gdashboard-cli-macos-x86_64"
)

ThisBuild / githubWorkflowOSes                  := binariesMatrix.keys.toSeq
ThisBuild / githubWorkflowTargetBranches        := Seq("main")
ThisBuild / githubWorkflowTargetTags           ++= Seq("v*")
ThisBuild / githubWorkflowPublish               := Nil
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowBuildPostamble ++=
  binariesMatrix.toSeq.flatMap { case (os, binaryName) =>
    val condition = s"startsWith(github.ref, 'refs/tags/v') && matrix.os == '$os'"
    Seq(
      WorkflowStep.Sbt(
        List(s"generateNativeBinary ./$binaryName"),
        name = Some(s"Generate $os native binary"),
        cond = Some(condition)
      ),
      WorkflowStep.Use(
        UseRef.Public("softprops", "action-gh-release", "v1"),
        name = Some(s"Upload $binaryName"),
        params = Map("files" -> binaryName),
        cond = Some(condition)
      )
    )
  }

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.native, cli.jvm, cli.native)
  .settings(name := "gdashboard")
  .settings(generateBinarySettings)

lazy val cli = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("./modules/cli"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "gdashboard-cli",
    libraryDependencies ++= Seq(
      "com.monovore" %%% "decline-effect"      % "2.4.1",
      "co.fs2"       %%% "fs2-io"              % "3.6.1",
      "org.http4s"   %%% "http4s-ember-client" % "0.23.18",
      "org.http4s"   %%% "http4s-circe"        % "0.23.18"
    ),
    buildInfoPackage := "io.gdashboard",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys    := Seq[BuildInfoKey](version)
  )
  .nativeSettings(
    libraryDependencies += "com.armanbilge" %%% "epollcat" % "0.1.4" // tcp for fs2
  )
  .dependsOn(core)

lazy val core = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("./modules/core"))
  .settings(
    name := "gdashboard-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect"   % "3.4.8",
      "io.circe"      %%% "circe-core"    % "0.14.5",
      "io.circe"      %%% "circe-generic" % "0.14.5",
      "io.circe"      %%% "circe-parser"  % "0.14.5",
      "io.scalaland"  %%% "chimney"       % "0.7.1"
    )
  )

lazy val generateBinarySettings = {
  val generateNativeBinary = inputKey[Unit]("Generate native binary")

  Seq(
    generateNativeBinary := {
      val log    = streams.value.log
      val args   = sbt.complete.Parsers.spaceDelimited("<arg>").parsed
      val binary = (cli.native / Compile / nativeLink).value
      val output = file(args.headOption.getOrElse("./gdashboard-cli"))

      log.info(s"Writing binary to $output")
      IO.delete(output)
      IO.copyFile(binary, output)
    }
  )
}
