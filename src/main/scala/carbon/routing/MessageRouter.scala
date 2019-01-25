package carbon.routing

import carbon.model.{Message, Target}

trait MessageRouter {
  def route(message: Message)
}

class StdOutMessageRouter(targets: Iterable[Target]) extends MessageRouter {

  private val roundRobinTargets = Iterator.continually(targets).flatten

  override def route(message: Message): Unit = {
    val target = roundRobinTargets.next()
    println(s"Routing message '${message.metricName}' to '${target.name}'")
  }
}
