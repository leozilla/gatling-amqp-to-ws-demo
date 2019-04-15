package betr.performance.amqp

import io.gatling.core.session.Expression

class Amqp(requestName: Expression[String], protocol: AmqpProtocol) {

  def createConnection(): AmqpClientRequestBuilder = new AmqpClientRequestBuilder(requestName, protocol.client, _.createConnection())

  def createChannel(): AmqpClientRequestBuilder = new AmqpClientRequestBuilder(requestName, protocol.client, _.createChannel())

  def publish(exchange: String, routingKey: String, body: String): AmqpPublishAndWsSendTextFrameBuilder =
    AmqpPublishAndWsSendTextFrameBuilder(requestName, PublishMessage(exchange, routingKey, body), protocol.client, Nil)
}

object Amqp {
  def apply(requestName: Expression[String])(implicit protocol: AmqpProtocol) = new Amqp(requestName, protocol)
}

case class PublishMessage(exchange: String, routingKey: String, msg: String)
