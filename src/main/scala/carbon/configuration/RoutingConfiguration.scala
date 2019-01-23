package carbon.configuration

import carbon.model.Target
import com.typesafe.config.Config

import scala.collection.JavaConverters._

case class RoutingConfiguration(processCount: Int, targets: Iterable[Target])

object RoutingConfiguration {
  def mkRoutingConfiguration(routingConfiguration: Config): RoutingConfiguration = {
    RoutingConfiguration(
      routingConfiguration.getInt("process_count"),
      routingConfiguration.getStringList("targets").asScala.map(Target.apply)
    )
  }
}
