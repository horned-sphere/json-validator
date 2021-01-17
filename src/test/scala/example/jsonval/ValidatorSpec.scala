package example.jsonval

import org.scalatest._

class ValidatorSpec extends WordSpec with Matchers with Inside {

  "The schema validator" should {

    "succeed for a valid schema and matching document" in {

      val validator = Validator.default()

      val result = validator.validate(Examples.ValidSchema, Examples.GoodDocument)

      assert(result.isRight)
    }

    "indicate an invalid schema with the appropriate error type" in {

      val validator = Validator.default()

      val result = validator.validate(Examples.BadSchema, Examples.GoodDocument)

      inside(result) {
        case Left(InvalidSchema(_)) =>
        case _ => fail("Unexpected result.")
      }
    }

    "fail for a document that does not match the schema" in {

      val validator = Validator.default()

      val result = validator.validate(Examples.ValidSchema, Examples.BadDocument)

      inside(result) {
        case Left(ValidationFailed(messages)) =>
          assert(messages.nonEmpty)
          println(messages)
        case _ => fail("Unexpected result.")
      }
    }

  }

}
