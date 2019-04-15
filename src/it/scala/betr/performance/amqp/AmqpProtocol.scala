package betr.performance.amqp

case class AmqpProtocolBuilder(protocol: AmqpProtocol) {

  def fromConfig(cfg: AmqpConfig): AmqpProtocolBuilder =
    copy(protocol = new AmqpProtocol(Some(cfg)))

  def build: AmqpProtocol = protocol.build()
}

class AmqpProtocol(config: Option[AmqpConfig] = None) {
  var client: RabbitMqClient = _

  def build(): AmqpProtocol = {
    client = new RabbitMqClient(config.get)
    this
  }

  def close(): Unit = client.close()
}
