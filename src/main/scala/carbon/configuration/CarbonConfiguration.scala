package carbon.configuration

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable

case class CarbonConfiguration(sockets: Iterable[SocketConfiguration])

object CarbonConfiguration {
  def load(): CarbonConfiguration = {
    val config = ConfigFactory.load()

    CarbonConfiguration(
      mkSocketConfiguration(config.getConfig("carbon.relay.socket"))
    )
  }

  private def mkSocketConfiguration(socketConfigs: Config): Iterable[SocketConfiguration] = {
    val buffer = mutable.Buffer[SocketConfiguration]()
    if (socketConfigs.hasPath("udp")) {
      val udpConfig = socketConfigs.getConfig("udp")
      buffer += Udp(udpConfig.getInt("port"))
    }
    if (socketConfigs.hasPath("tcp")) {
      val tcpConfig = socketConfigs.getConfig("tcp")
      buffer += Tcp(tcpConfig.getInt("port"))
    }
    buffer
  }
}
