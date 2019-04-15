package betr.performance.amqp

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation._
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

import scala.util.Try

case class AmqpClientRequestBuilder(requestName: Expression[String], client: RabbitMqClient, clientRequest: RabbitMqClient => Unit)

object AmqpClientRequestBuilder {

  implicit def toActionBuilder(requestBuilder: AmqpClientRequestBuilder): AmqpClientActionBuilder =
    AmqpClientActionBuilder(requestBuilder)
}

case class AmqpClientActionBuilder(requestBuilder: AmqpClientRequestBuilder) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = {
    new AmqpClientAction(requestBuilder.requestName,
                         ctx.coreComponents.statsEngine,
                         ctx.coreComponents.clock,
                         requestBuilder.client,
                         requestBuilder.clientRequest,
                         next)
  }
}

class AmqpClientAction(override val requestName: Expression[String],
                       override val statsEngine: StatsEngine,
                       override val clock: Clock,
                       client: RabbitMqClient,
                       clientRequest: RabbitMqClient => Unit,
                       val next: Action)
    extends RequestAction
    with NameGen {

  override def sendRequest(requestName: String, session: Session): Validation[Unit] = {
    val start = clock.nowMillis
    val v = Try {
      clientRequest(client)
    }.toValidation
    val end = clock.nowMillis

    val (status, maybeMsg) = v match {
      case Success(_)   => (OK, None)
      case Failure(msg) => (KO, Some(msg))
    }

    val newSessionWithMark = if (status == KO) session.markAsFailed else session
    statsEngine.logResponse(newSessionWithMark, requestName, start, end, status, None, maybeMsg)
    next.execute(newSessionWithMark)

    v
  }

  override def name: String = genName("amqpClient")
}
