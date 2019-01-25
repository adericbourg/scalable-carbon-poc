package carbon.parsing

import carbon.model.Message
import org.scalatest.{FunSuite, Matchers}

class MetricParserTest extends FunSuite with Matchers {

  test("parseOne should parse valid data one by one") {
    MetricParser.parseOne("foo 3 1234") shouldBe Some(Message("foo", 3, 1234))
    MetricParser.parseOne("foo.bar 4.0 123") shouldBe Some(Message("foo.bar", 4.0, 123))
  }

  test("parse should parse valid batched data") {
    MetricParser.parse(
      """
        |foo 3 1234
        |foo.bar 4.0 123
      """.stripMargin) shouldBe Seq(
      Message("foo", 3, 1234),
      Message("foo.bar", 4.0, 123)
    )
  }

  test("parseOne should ignore invalid data") {
    MetricParser.parseOne("") shouldBe None
    MetricParser.parseOne("foo") shouldBe None
    MetricParser.parseOne("foo bar baz") shouldBe None
  }

  test("parse should ignore invalid data") {
    MetricParser.parse(
      """
        |foo 3 1234
        |foo.bar 4.0 123
        |foo
        |foo bar
      """.stripMargin) shouldBe Seq(
      Message("foo", 3, 1234),
      Message("foo.bar", 4.0, 123)
    )
  }
}
