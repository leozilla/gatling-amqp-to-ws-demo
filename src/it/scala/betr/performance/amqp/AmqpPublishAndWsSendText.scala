package betr.performance.amqp

import com.softwaremill.quicklens._
import io.gatling.commons.util.Clock
import io.gatling.core.Predef._
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.http.action.ws.WsSendTextFrame
import io.gatling.http.check.ws.{WsFrameCheckSequence, WsTextFrameCheck}

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

case class AmqpPublishAndWsSendTextFrameBuilder(requestName: Expression[String],
                                                msg: PublishMessage,
                                                amqpClient: RabbitMqClient,
                                                checkSequences: List[WsFrameCheckSequence[WsTextFrameCheck]])
    extends ActionBuilder {

  private val wsName      = "gatling.http.webSocket"
  private val wsMsgToSend = "test"

  def await(timeout: FiniteDuration)(checks: WsTextFrameCheck*): AmqpPublishAndWsSendTextFrameBuilder =
    this.modify(_.checkSequences).using(_ ::: List(WsFrameCheckSequence(timeout, checks.toList)))

  override def build(ctx: ScenarioContext, next: Action): Action = {
    new AmqpPublishMessage(
      requestName,
      "amqpPublish_wsSendTextFrame",
      msg,
      ctx.coreComponents.statsEngine,
      ctx.coreComponents.clock,
      next = next,
      amqpClient,
      new WsSendTextFrame(
        requestName,
        wsName,
        wsMsgToSend,
        checkSequences,
        ctx.coreComponents.statsEngine,
        ctx.coreComponents.clock,
        next = next
      )
    )
  }
}

class AmqpPublishMessage(val requestName: Expression[String],
                         baseName: String,
                         message: Expression[PublishMessage],
                         override val statsEngine: StatsEngine,
                         override val clock: Clock,
                         override val next: Action,
                         amqpClient: RabbitMqClient,
                         innerAction: Action)
    extends ExitableAction
    with NameGen {

  override def execute(session: Session): Unit = recover(session) {
    requestName(session).flatMap { resolvedRequestName =>
      val outcome =
        try {
          message(session).map { msg =>
            innerAction.execute(session)                              // first trigger sending of WS message (this just sends a msg to the WsActor)
            amqpClient.publish(msg.exchange, msg.routingKey, msg.msg) // then publish
          }
        } catch {
          case NonFatal(e) =>
            statsEngine.reportUnbuildableRequest(session, resolvedRequestName, e.getMessage)
            // rethrow so we trigger exception handling in "!"
            throw e
        }
      outcome.onFailure { errorMessage =>
        statsEngine.reportUnbuildableRequest(session, resolvedRequestName, errorMessage)
      }
      outcome
    }
  }

  override def name: String = genName(baseName)
}
