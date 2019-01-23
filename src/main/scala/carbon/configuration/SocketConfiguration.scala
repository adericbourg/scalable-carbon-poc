package carbon.configuration

import com.typesafe.config.Config

import scala.collection.mutable

sealed trait SocketConfiguration

final case class Tcp(port: Int) extends SocketConfiguration

final case class Udp(port: Int) extends SocketConfiguration

object SocketConfiguration {
  def mkSocketConfiguration(socketConfigs: Config): Iterable[SocketConfiguration] = {
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