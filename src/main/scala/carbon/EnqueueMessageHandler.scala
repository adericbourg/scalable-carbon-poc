package carbon

import java.util

import carbon.model.Message
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.util.CharsetUtil

class EnqueueMessageHandler(queue: util.Queue[Message]) extends SimpleChannelInboundHandler[DatagramPacket] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket): Unit = {
    val raw = msg.content().toString(CharsetUtil.UTF_8)
    val message = Message(raw)
    queue.add(message)
  }
}

import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.util.CharsetUtil

class EnqueueTcpMessageHandler(queue: util.Queue[Message]) extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val raw = msg.asInstanceOf[ByteBuf].toString(CharsetUtil.UTF_8)
    val message = Message(raw)
    queue.add(message)
  }
}