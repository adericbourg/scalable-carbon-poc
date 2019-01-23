package carbon

import java.util.concurrent.{ArrayBlockingQueue, Executors}

import carbon.configuration._
import carbon.model.Message
import carbon.routing.{QueueMessageConsumer, StdOutMessageRouter}
import carbon.socket.{TcpSocket, UdpSocket}

object Run extends App {

  val configuration = CarbonConfiguration.load()
  val inboundQueue = new ArrayBlockingQueue[Message](configuration.routingConfiguration.maxItemsInQueue)

  val messageRouter = new StdOutMessageRouter(configuration.routingConfiguration.targets)
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

  val sockets = configuration.sockets.map {
    case Udp(port) => new UdpSocket(port, inboundQueue)
    case Tcp(port) => new TcpSocket(port, inboundQueue)
  }

  sockets.foreach { socket =>
    socket.bind()
  }

  println("Carbon relay started...")
}
