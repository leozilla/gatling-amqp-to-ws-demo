package betr.performance.contracts

import java.time.Instant
import java.util.UUID

object Amqp {

  def event[P <: AmqpMessagePayload](payload: P): RabbitMqEnvelope[P] = {
    RabbitMqEnvelope(UUID.randomUUID().toString,
                     "betr.performance-test-driver",
                     UUID.randomUUID().toString,
                     Instant.now(),
                     payload.eventType,
                     1,
                     payload)
  }
}

trait AmqpMessagePayload {
  def eventType: String
}
