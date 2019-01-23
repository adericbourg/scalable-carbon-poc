package carbon.configuration

sealed trait SocketConfiguration

final case class Tcp(port: Int) extends SocketConfiguration

final case class Udp(port: Int) extends SocketConfiguration
