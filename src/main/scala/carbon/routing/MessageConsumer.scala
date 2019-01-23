package carbon.routing

import java.util.concurrent.BlockingQueue

import carbon.model.Message

import scala.annotation.tailrec

trait MessageConsumer {
  def consume(): Unit
}

// TODO empty queues before completing shutdown?
class QueueMessageConsumer(inboundQueue: BlockingQueue[Message],
                           router: MessageRouter) extends MessageConsumer {
  @tailrec
  final override def consume(): Unit = {
    val message = inboundQueue.take()
    router.route(message)
    consume()
  }
}
