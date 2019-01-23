package carbon.configuration

import com.typesafe.config.ConfigFactory

case class CarbonConfiguration(sockets: Iterable[SocketConfiguration],
                               routingConfiguration: RoutingConfiguration)

object CarbonConfiguration {
  def load(): CarbonConfiguration = {
    val config = ConfigFactory.load()

    CarbonConfiguration(
      SocketConfiguration.mkSocketConfiguration(config.getConfig("carbon.relay.socket")),
      RoutingConfiguration.mkRoutingConfiguration(config.getConfig("carbon.relay.routing"))
    )
  }
}
