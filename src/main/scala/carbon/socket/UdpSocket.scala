package carbon.socket

import java.util
import java.util.concurrent.BlockingQueue

import carbon.model.Message
import carbon.parsing.MetricParser
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
        ch.pipeline().addLast(new UdpSocketMessageHandler(inboundQueue))
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

class UdpSocketMessageHandler(queue: util.Queue[Message]) extends SimpleChannelInboundHandler[DatagramPacket] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket): Unit = {
    val raw = msg.content().toString(CharsetUtil.UTF_8)
    MetricParser.parse(raw).foreach(queue.add)
  }
}