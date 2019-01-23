package carbon.configuration

import com.typesafe.config.Config

case class RoutingConfiguration(processCount: Int)

object RoutingConfiguration {
  def mkRoutingConfiguration(routingConfiguration: Config): RoutingConfiguration = {
    RoutingConfiguration(
      routingConfiguration.getInt("process_count")
    )
  }
}