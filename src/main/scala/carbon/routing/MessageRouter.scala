package carbon.routing

import carbon.model.Message

trait MessageRouter {
  def route(message: Message)
}

class StdOutMessageRouter extends MessageRouter {
  override def route(message: Message): Unit = {
    println(s"Routing message '${message.raw}'")
  }
}
