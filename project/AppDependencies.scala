import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.11.0"
  val mongoVersion     = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"               %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-play-30"        % mongoVersion,
    "com.github.java-json-tools" % "json-schema-validator"     % "2.2.14"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"  %% "mockito-4-11"              % "3.2.18.0"       % "test",
    "uk.gov.hmrc"        %% "bootstrap-test-play-30"    % bootstrapVersion % "test",
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.5.3"          % Test
  )
}
