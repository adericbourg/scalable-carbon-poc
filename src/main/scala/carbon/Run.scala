package carbon

import carbon.configuration._
import carbon.socket.{TcpSocket, UdpSocket}

object Run extends App {
  val configuration = CarbonConfiguration.load()

  val sockets = configuration.sockets.map {
    case Udp(port) => new UdpSocket(port)
    case Tcp(port) => new TcpSocket(port)
  }

  sockets.foreach { socket =>
    socket.bind()
  }

  println("Carbon relay started")
}
