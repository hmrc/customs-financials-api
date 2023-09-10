package services.cache

import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodProcess, MongodStarter}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.process.runtime.Network

import scala.util.Random

trait EmbeddedMongoDBSupport {

  val mongoHost = "localhost"
  val mongoPort = 10000 + Random.nextInt(10000)

  var mongodExecutable: MongodExecutable = _

  def startMongoD(): MongodProcess =
    mongodExecutable.start()

  def stopMongoD(): Unit =
    mongodExecutable.stop()

  def initMongoDExecutable(): Unit =
    mongodExecutable = MongodStarter.getDefaultInstance
      .prepare(MongodConfig.builder()
        .version(Version.Main.V4_4)
        .net(new Net(mongoHost, mongoPort, Network.localhostIsIPv6()))
        .build()
      )
}