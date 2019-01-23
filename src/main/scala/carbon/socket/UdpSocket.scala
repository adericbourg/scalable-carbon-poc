package carbon.socket

import java.util
import java.util.concurrent.{BlockingQueue, TimeUnit}

import carbon.model.Message
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.util.CharsetUtil

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
    val channelFuture = bootstrap.bind(port).sync()
    sys.addShutdownHook {
      println("Starting UDP socket shutdown...")
      channelFuture.channel().closeFuture().sync()
      workerGroup.shutdownGracefully(0, 30, TimeUnit.SECONDS)
      println("UDP socket gracefully shutdown :)")
    }
  }
}

class EnqueueMessageHandler(queue: util.Queue[Message]) extends SimpleChannelInboundHandler[DatagramPacket] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket): Unit = {
    val raw = msg.content().toString(CharsetUtil.UTF_8)
    val message = Message(raw)
    queue.add(message)
  }
}
