import play.core.PlayVersion
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.16.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.56.0",
    "ru.tinkoff"        %% "phobos-core"               % "0.9.2"

  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test",
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % "test",
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8"
  )
}
