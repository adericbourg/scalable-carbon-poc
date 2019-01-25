package carbon.parsing

import carbon.model.Message

import scala.util.Try

object MetricParser {
  def parse(in: String): Seq[Message] = in.split("\n").flatMap(parseOne)

  def parseOne(in: String): Option[Message] = {
    in.split(" ") match {
      case Array(metricName, value, timestamp) => buildMessage(metricName, value, timestamp)
      case _                                   => None
    }
  }

  private def buildMessage(values: (String, String, String)): Option[Message] = {
    val (metricName, value, timestamp) = values
    Try {
      Message(metricName, value.toDouble, timestamp.toInt)
    }.toOption
  }
}

