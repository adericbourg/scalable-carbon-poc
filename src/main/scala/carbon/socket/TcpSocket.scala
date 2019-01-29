package carbon.socket

import java.util

import carbon.model.Message
import carbon.parsing.MetricParser
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer}
import io.netty.util.CharsetUtil

class TcpSocket(port: Int, inboundQueue: util.Queue[Message]) extends CarbonSocket {
  private val workerGroup = new NioEventLoopGroup()

  private val bootstrap = new ServerBootstrap()
  bootstrap.group(workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ChannelInitializer[NioSocketChannel] {
      override def initChannel(ch: NioSocketChannel): Unit = {
        ch.pipeline().addLast(new TcpSocketMessageHandler(inboundQueue))
      }
    })

  def bind(): Unit = {
    println(s"Binding TCP listener on port $port")
    val channelFuture = bootstrap.bind(port).sync()
    sys.addShutdownHook {
      println("Starting TCP socket shutdown...")
      channelFuture.channel().close()
      workerGroup.shutdownGracefully().sync()
      println("TCP socket gracefully shutdown :)")
    }
  }
}


class TcpSocketMessageHandler(queue: util.Queue[Message]) extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val raw = msg.asInstanceOf[ByteBuf].toString(CharsetUtil.UTF_8)
    MetricParser.parse(raw).foreach(queue.add)
  }
}
