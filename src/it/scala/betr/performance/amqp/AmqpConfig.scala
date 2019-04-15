package betr.performance.amqp

import betr.performance.AppConfigLoader
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString // required!

case class AmqpConfig(host: NonEmptyString,
                      port: Option[PortNumber],
                      vhost: NonEmptyString,
                      username: NonEmptyString,
                      password: NonEmptyString,
                      exchange: AmqpExchangeConfig) {
  val amqpUri: String = port match {
    case Some(p) => s"amqp://${username.value}:${password.value}@${host.value}:${p.value}"
    case None    => s"amqp://${username.value}:${password.value}@${host.value}"
  }
}

case class AmqpExchangeConfig(name: NonEmptyString)

object AmqpConfig {
  import pureconfig.generic.auto._
  import eu.timepit.refined.pureconfig._ // required!

  val instance: AmqpConfig = AppConfigLoader.load[AmqpConfig]("amqp")
}
