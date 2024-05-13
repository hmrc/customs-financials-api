import scoverage.ScoverageKeys
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "customs-financials-api"

val scala2_13_12 = "2.13.12"
val bootstrapVersion = "8.6.0"
val silencerVersion = "1.7.16"

val testDirectory = "test"
val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_12

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test))

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value / scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / testScalaStyleConfigFile)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(PlayKeys.playDefaultPort := 9878)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    ScoverageKeys.coverageExcludedPackages := List("<empty>"
      , ".*Reverse.*"
      , ".*services.dec64.Dec64Headers.*"
      , ".*(BuildInfo|Routes|testOnly).*").mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageMinimumBranchTotal := 89,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-P:silencer:pathFilters=target/.*",
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),
    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates")
  )
  .settings(scalastyleSettings)
  .settings(Test / parallelExecution := false)
  .settings(resolvers += Resolver.jcenterRepo)
