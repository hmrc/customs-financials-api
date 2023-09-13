import play.core.PlayVersion
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.19.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.3.0",
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.2.16" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test",
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % "test",
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % "test",
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % "test",
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.5.3" % Test,
  )
}
