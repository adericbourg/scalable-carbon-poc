package carbon.socket

import java.util.concurrent.BlockingQueue

import carbon.EnqueueMessageHandler
import carbon.model.Message
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel

class UdpSocket(port: Int, inboundQueue: BlockingQueue[Message]) extends CarbonSocket {

  private val workerGroup = new NioEventLoopGroup()

  private val bootstrap = new Bootstrap()
  bootstrap.group(workerGroup)
    .channel(classOf[NioDatagramChannel])
    .handler(new ChannelInitializer[NioDatagramChannel] {
      override def initChannel(ch: NioDatagramChannel): Unit = {
        ch.pipeline().addLast(new EnqueueMessageHandler(inboundQueue))
      }
    })

  def bind(): Unit = {
    println(s"Binding UDP listener on port $port")
    val channelFuture = bootstrap.bind(port).sync()
    sys.addShutdownHook {
      println("Starting UDP socket shutdown...")
      channelFuture.channel().closeFuture().sync()
      workerGroup.shutdownGracefully().sync()
      println("UDP socket gracefully shutdown :)")
    }
  }
}
