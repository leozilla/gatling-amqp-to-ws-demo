package betr.performance.contracts.ssln

import java.time.Instant

import betr.performance.contracts.AmqpMessagePayload

object FixtureDtos {

  sealed trait FixtureStatus
  object FixtureStatus {
    case object Created   extends FixtureStatus
    case object Active    extends FixtureStatus
    case object Suspended extends FixtureStatus
    case object Closed    extends FixtureStatus
    case object Resulted  extends FixtureStatus
  }

  case class FixtureUpdated(id: String,
                            startTime: Instant,
                            status: FixtureStatus)
      extends AmqpMessagePayload {
    override def eventType: String = "FixtureUpdated"
  }
}
