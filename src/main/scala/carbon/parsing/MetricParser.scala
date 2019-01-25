package carbon.parsing

import carbon.model.Message
import fastparse.NoWhitespace._
import fastparse._

object MetricParser {

  def parse(in: String): Seq[Message] = in.split("\n").flatMap(parseOne)

  def parseOne(in: String): Option[Message] = {
    fastparse.parse(in, message(_)) match {
      case Parsed.Success(result, _) => Some(result)
      case _                         => None // TODO log.debug error
    }
  }

  private def message[_: P]: P[Message] = P(metricName ~ space ~ value ~ space ~ timestamp ~ End)
    .map(tuple => Message(tuple._1, tuple._2, tuple._3))

  private def space[_: P]: P[Unit] = P(CharIn(" ").rep(1))

  private def metricName[_: P]: P[String] = P(((CharIn("a-zA-Z").rep(1) ~ ".").rep(0) ~ CharIn("a-zA-Z").rep(1)).!)

  private def timestamp[_: P]: P[Long] = P(sign ~ digit.rep(1).!).map(_.toLong)

  private def value[_: P]: P[Double] = {
    def withPoint = P(digit.rep(0) ~ "." ~ digit.rep(1))

    def withoutPoint = P(digit.rep(1))

    P((sign ~ (withPoint | withoutPoint)).!).map(_.toDouble)
  }

  private def sign[_: P] = P(CharIn("+\\-").?)

  private def digit[_: P] = P(CharIn("0-9"))

}
