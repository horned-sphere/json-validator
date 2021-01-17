package example.jsonval.response

import org.scalatest.{Matchers, WordSpec}
import io.circe.syntax._
import io.circe.literal._

class ResponseSpec extends WordSpec with Matchers {

  "A schema endpoint response" can {
    "valid" should {

      "produce the expected JSON response" in {

        val response = SchemaResponse.goodUpload("schemaName")

        val jsonResponse = response.asJson

        val expected =
          json"""
                 {
                    "action": "uploadSchema",
                    "id": "schemaName",
                    "status": "success"
                 }
            """

        jsonResponse shouldEqual expected
      }

    }

    "invalid" should {

      "produce the expected JSON response" in {

        val response = SchemaResponse.badUpload("schemaName")

        val jsonResponse = response.asJson

        val expected =
          json"""
                 {
                    "action": "uploadSchema",
                    "id": "schemaName",
                    "status": "error",
                    "message": "Invalid JSON"
                 }
            """

        jsonResponse shouldEqual expected
      }

    }
  }

  "A validation endpoint response" can {

    "valid" should {

      "produce the expected JSON response" in {

        val response = ValidationResponse.valid("schemaName")

        val jsonResponse = response.asJson

        val expected =
          json"""
                 {
                    "action": "validateDocument",
                    "id": "schemaName",
                    "status": "success"
                 }
            """

        jsonResponse shouldEqual expected
      }

    }

    "invalid" should {

      "produce the expected JSON response" in {

        val response = ValidationResponse.failed("schemaName", List("message1", "message2"))

        val jsonResponse = response.asJson

        val expected =
          json"""
                 {
                    "action": "validateDocument",
                    "id": "schemaName",
                    "status": "error",
                    "message": ["message1", "message2"]
                 }
            """

        jsonResponse shouldEqual expected
      }

    }

  }

}
