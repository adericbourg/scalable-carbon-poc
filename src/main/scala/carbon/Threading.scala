package carbon

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentHashMap, ThreadFactory}

object Threading {
  private val threadNumbers = new ConcurrentHashMap[String, AtomicInteger]()

  def namedThreadFactory(prefix: String): ThreadFactory = new NamedThreadFactory(prefix)

  private[Threading] def getNewTheadNumber(prefix: String): Int = {
    threadNumbers.computeIfAbsent(prefix, _ => new AtomicInteger(1)).getAndIncrement()
  }

  class NamedThreadFactory(prefix: String) extends ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val thread = new Thread(r, s"$prefix-${getNewTheadNumber(prefix)}")
      thread.setDaemon(true)
      thread.setPriority(Thread.NORM_PRIORITY)
      thread
    }
  }

}
