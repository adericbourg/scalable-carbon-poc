package carbon.socket

import java.util

import carbon.model.Message
import carbon.parsing.MetricParser
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer}
import io.netty.util.{AttributeKey, CharsetUtil}

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
  private val buffer: AttributeKey[String] = AttributeKey.valueOf("buffer");
  private val lineDelimiter = '\n'

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    var receiveBuffer = ctx.attr(buffer).get();
    if (receiveBuffer == null) {
      receiveBuffer = ""
    }

    // append last message with current message
    val raw = receiveBuffer + msg.asInstanceOf[ByteBuf].toString(CharsetUtil.UTF_8)

    // split at the last new line
    val lines = raw splitAt (raw lastIndexOf lineDelimiter)

    // incomplete line goes to the buffer
    ctx.attr(buffer).set(lines._2.substring(1))

    // complete lines are parsed and routed
    MetricParser.parse(lines._1).foreach(queue.add)
  }
  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    var receiveBuffer = ctx.attr(buffer).get();
    if (receiveBuffer != null) {
      MetricParser.parse(receiveBuffer).foreach(queue.add)
    }
  }
}
