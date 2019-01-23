package carbon.socket

import java.util

import carbon.EnqueueTcpMessageHandler
import carbon.model.Message
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}

class TcpSocket(port: Int, inboundQueue: util.Queue[Message]) extends CarbonSocket {
  private val workerGroup = new NioEventLoopGroup()

  private val bootstrap = new ServerBootstrap()
  bootstrap.group(workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(new ChannelInitializer[NioSocketChannel] {
      override def initChannel(ch: NioSocketChannel): Unit = {
        ch.pipeline().addLast(new EnqueueTcpMessageHandler(inboundQueue))
      }
    })

  def bind(): Unit = {
    println(s"Binding TCP listener on port $port")
    val channelFuture = bootstrap.bind(port).sync()
    sys.addShutdownHook {
      println("Starting TCP socket shutdown...")
      channelFuture.channel().closeFuture().sync()
      workerGroup.shutdownGracefully().sync()
      println("TCP socket gracefully shutdown :)")
    }
  }
}
