import play.core.PlayVersion
import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-29" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-29" % "1.7.0",
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.17" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % "test",
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.30" % "test",
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % "test",
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % "test",
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.5.3" % Test,
  )
}
