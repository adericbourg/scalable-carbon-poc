package carbon

import java.util.concurrent.{Executors, LinkedBlockingQueue}

import carbon.configuration._
import carbon.model.Message
import carbon.routing.{QueueMessageConsumer, StdOutMessageRouter}
import carbon.socket.{TcpSocket, UdpSocket}

object Run extends App {

  val configuration = CarbonConfiguration.load()
  val inboundQueue = new LinkedBlockingQueue[Message]()

  val sockets = configuration.sockets.map {
    case Udp(port) => new UdpSocket(port, inboundQueue)
    case Tcp(port) => new TcpSocket(port)
  }

  val messageRouter = new StdOutMessageRouter
  val routerPool = Executors.newFixedThreadPool(
    configuration.routingConfiguration.processCount,
    Threading.namedThreadFactory("routing-pool")
  )
  (1 to configuration.routingConfiguration.processCount).map(_ => new QueueMessageConsumer(inboundQueue, messageRouter))
    .foreach { consumer =>
      routerPool.submit(new Runnable {
        override def run(): Unit = consumer.consume()
      })
    }

  sockets.foreach { socket =>
    socket.bind()
  }

  println("Carbon relay started...")
}
