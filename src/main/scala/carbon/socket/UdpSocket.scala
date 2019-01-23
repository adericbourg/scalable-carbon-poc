package carbon.socket

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.util.CharsetUtil

class UdpSocket(port: Int) extends CarbonSocket {

  private val workerGroup = new NioEventLoopGroup()

  private val bootstrap = new Bootstrap()
  bootstrap.group(workerGroup)
    .channel(classOf[NioDatagramChannel])
    .handler(new ChannelInitializer[NioDatagramChannel] {
      override def initChannel(ch: NioDatagramChannel): Unit = {
        ch.pipeline().addLast(new ConsolePrintHandler)
      }
    })
  
  def bind(): Unit = {
    val channelFuture = bootstrap.bind(port).sync()
    sys.addShutdownHook {
      channelFuture.channel().closeFuture().sync()
      workerGroup.shutdownGracefully()
    }
  }
}

class ConsolePrintHandler extends SimpleChannelInboundHandler[DatagramPacket] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket): Unit = {
    print(msg.content().toString(CharsetUtil.UTF_8))
  }
}
