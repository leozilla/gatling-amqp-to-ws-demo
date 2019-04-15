package betr.performance

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import betr.performance.amqp.{AmqpConfig, AmqpDsl, AmqpProtocol}
import betr.performance.contracts.PublicApi._
import betr.performance.contracts.ssln.FixtureDtos.{FixtureStatus, FixtureUpdated}
import betr.performance.contracts.{Amqp, Ssln}
import betr.performance.steps.FixtureSteps._
import io.gatling.core.Predef._
import spray.json._

import scala.concurrent.duration._

class FixtureUpdatedSimulation extends PublicApiSimulation with AmqpDsl {

  val exchange: String = AmqpConfig.instance.exchange.name.value

  implicit val amqpProtocol: AmqpProtocol = amqp()
    .fromConfig(AmqpConfig.instance)
    .build

  val fixture = FixtureUpdated(UUID.randomUUID().toString, Instant.now(), FixtureStatus.Created)

  val fixtureRoutingKey: String = Ssln.fixtureUpdatedRoutingKey(fixture)

  val totalClients                          = 50
  val subscribedClients: AtomicInteger      = new AtomicInteger()
  val fixtureUpdatesReceived: AtomicBoolean = new AtomicBoolean()

  val subscribedForFixturesAndIdle = scenario("subscribed for fixtures")
    .exec(wsConnectToPublicApi())
    .exec(setNewRequestId(_))
    .exec(subscribedForFixtures())
    .exec(session => { subscribedClients.incrementAndGet(); session })
    .asLongAs(_ => !fixtureUpdatesReceived.get()) { // wait until the fixture updates where received, then client can disconnect
      pause(1.seconds, 5.seconds)
    }

  val fixtureUpdates = scenario("publish fixture updated event")
    .exec(wsConnectToPublicApi())
    .exec(amqp("Create AMQP connection").createConnection())
    .exec(amqp("Create AMQP channel").createChannel())
    .exec(setNewRequestId(_))
    .exec(subscribedForFixtures())
    .asLongAs(_ => subscribedClients.get() < totalClients) { // wait until all clients are subscribed, then trigger the fixture updates
      pause(1.seconds, .3 seconds)
    }
    //
    .exec(amqp("AMQP FixtureUpdated Created").publish(exchange, fixtureRoutingKey, Amqp.event(fixture).toJson.compactPrint))
    //
    .pause(3.second)
    //
    .exec(amqp("AMQP FixtureUpdated Active")
      .publish(exchange, fixtureRoutingKey, Amqp.event(fixture.copy(status = FixtureStatus.Active)).toJson.compactPrint)
      .await(5.seconds)(checkFixtureUpdated("FixtureUpdated Active", fixture.id, "ACTIVE")))
    //
    .pause(3.seconds)
    //
    .exec(amqp("AMQP FixtureUpdated Resulted")
      .publish(exchange, fixtureRoutingKey, Amqp.event(fixture.copy(status = FixtureStatus.Resulted)).toJson.compactPrint)
      .await(5.seconds)(checkFixtureUpdated("FixtureUpdated Resulted", fixture.id, "RESULTED")))
    .exec(session => { fixtureUpdatesReceived.set(true); session })

  setUp(
    fixtureUpdates.inject(atOnceUsers(1)),
    subscribedForFixturesAndIdle.inject(rampUsers(totalClients).during(1.minute))
  ).assertions(details("SubscribeFixtures response").responseTime.percentile(99).lte(2500))
    .assertions(details("FixtureUpdated Active").responseTime.percentile(99).lte(250))
    .assertions(details("FixtureUpdated Resulted").responseTime.percentile(99).lte(250))
    .protocols(wsProtocol)

  after {
    amqpProtocol.close()
  }
}
