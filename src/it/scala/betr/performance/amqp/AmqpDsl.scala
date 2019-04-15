package betr.performance.amqp

import io.gatling.core.session.Expression

trait AmqpDsl {

  def amqp(requestName: Expression[String])(implicit protocol: AmqpProtocol) = new Amqp(requestName, protocol)

  def amqp() = AmqpProtocolBuilder(new AmqpProtocol())
}
