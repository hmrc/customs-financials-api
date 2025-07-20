import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings
import AppDependencies.bootstrapVersion

val appName = "customs-financials-api"

val scala3_3_6      = "3.3.6"
val silencerVersion = "1.7.16"

val testDirectory            = "test"
val scalaStyleConfigFile     = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala3_3_6

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test))

lazy val scalastyleSettings = Seq(
  scalastyleConfig := baseDirectory.value / scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / testScalaStyleConfigFile
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(PlayKeys.playDefaultPort := 9878)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencies ++= Seq(
      compilerPlugin(
        "com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.for3Use2_13With("", ".12")
      ),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.for3Use2_13With("", ".12")
    ),
    ScoverageKeys.coverageExcludedPackages := List(
      "<empty>",
      ".*Reverse.*",
      ".*services.dec64.Dec64Headers.*",
      ".*(BuildInfo|Routes|testOnly).*",
      ".*HistoricDocumentRequestSearchCache*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    scalacOptions += "-Wconf:msg=Flag.*repeatedly:s",
    Test / scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    scalafmtDetailedError := true,
    scalafmtPrintDiff := true,
    scalafmtFailOnErrors := true
  )
  .settings(scalastyleSettings)
  .settings(Test / parallelExecution := false)

addCommandAlias(
  "runAllChecks",
  ";clean;compile;coverage;test;it/test;scalafmtCheckAll;scalastyle;Test/scalastyle;coverageReport"
)
