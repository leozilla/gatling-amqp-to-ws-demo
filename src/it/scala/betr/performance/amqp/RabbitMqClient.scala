package betr.performance.amqp

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.blocking

class RabbitMqClient(config: AmqpConfig) {
  private val logger = LoggerFactory.getLogger(classOf[RabbitMqClient])

  var connection: Connection = _
  var channel: Channel       = _

  def createConnection(): Unit = {
    val connectionFactory = new ConnectionFactory
    connectionFactory.setHost(config.host.value)
    connectionFactory.setUsername(config.username.value)
    connectionFactory.setPassword(config.password.value)
    config.port.foreach(p => connectionFactory.setPort(p.value))
    connectionFactory.setVirtualHost(config.vhost.value)

    logger.info("AMQP creating connection")
    blocking {
      connection = connectionFactory.newConnection("betr.performance-test-driver")
    }
    logger.debug("AMQP connected")
  }

  def createChannel(): Unit = {
    logger.info("AMQP creating channel")
    blocking {
      channel = connection.createChannel()
    }
    logger.debug("AMQP channel created")
  }

  def publish(exchange: String, routingKey: String, msg: String): Unit = {
    logger.info("AMQP publishing: {}", msg)
    blocking {
      channel.basicPublish(exchange, routingKey, null, msg.getBytes("UTF-8"))
    }
    logger.debug("AMQP message published")
  }

  def close(): Unit = {
    logger.info("AMQP closing channel and connection")
    blocking {
      channel.close()
      connection.close()
    }
  }
}
