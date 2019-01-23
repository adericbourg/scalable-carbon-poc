package carbon

import java.util

import carbon.model.Message
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.channel.socket.DatagramPacket
import io.netty.util.CharsetUtil

class EnqueueMessageHandler(queue: util.Queue[Message]) extends SimpleChannelInboundHandler[DatagramPacket] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket): Unit = {
    val raw = msg.content().toString(CharsetUtil.UTF_8)
    val message = Message(raw)
    queue.add(message)
  }
}