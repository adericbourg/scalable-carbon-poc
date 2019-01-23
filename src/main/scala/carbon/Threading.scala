package carbon

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentHashMap, ThreadFactory}

object Threading {
  private val threadNumbers = new ConcurrentHashMap[String, AtomicInteger]()

  def getNewTheadNumber(prefix: String): Int = {
    threadNumbers.computeIfAbsent(prefix, _ => new AtomicInteger(1)).getAndIncrement()
  }

  def namedThreadFactory(prefix: String): ThreadFactory = new NamedThreadFactory(prefix)
}

class NamedThreadFactory(prefix: String) extends ThreadFactory {

  import Threading._

  override def newThread(r: Runnable): Thread = {
    val thread = new Thread(r, s"$prefix-${getNewTheadNumber(prefix)}")
    thread.setDaemon(true)
    thread.setPriority(Thread.NORM_PRIORITY)
    thread
  }
}
