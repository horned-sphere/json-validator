package example.jsonval

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.scalatest._
import io.circe.literal._

class ModelSpec extends WordSpec with Matchers {

  "The Circe to Jackson model folder" can {

    "not stripping null fields" should {

      "produce an equivalent Jackson node" in {
        val doc =
          json"""{
              "number": 3,
              "text": "stuff",
              "bad": null,
              "array": [1, 2, 3],
              "nested": { "inner": "more" }
            }"""

        val fac = JsonNodeFactory.instance

        val jackson = doc.foldWith(Model.toJacksonFolder(fac, removeNullFields = false))
        assert(jackson.isObject)
        jackson.size() shouldEqual 5
        assert(jackson.get("number").isNumber)
        jackson.get("number").asInt() shouldEqual 3
        jackson.get("text") shouldEqual fac.textNode("stuff")
        jackson.get("bad") shouldEqual fac.nullNode()
        val arrNode = jackson.get("array")
        assert(arrNode.isArray)
        arrNode.size() shouldEqual 3
        assert(arrNode.get(0).isNumber)
        assert(arrNode.get(1).isNumber)
        assert(arrNode.get(2).isNumber)
        arrNode.get(0).asInt shouldEqual 1
        arrNode.get(1).asInt shouldEqual 2
        arrNode.get(2).asInt shouldEqual 3
        val nestedNode = jackson.get("nested")
        assert(nestedNode.isObject)
        nestedNode.size() shouldEqual 1
        nestedNode.get("inner") shouldEqual fac.textNode("more")
      }

    }


    "stripping null fields" should {

      "produce a Jackson node with null fields removed" in {
        val doc =
          json"""{
              "good": 3,
              "bad": null,
              "array": [1, null, 3],
              "nested": {
                "good": 3,
                "bad": null
               }
            }"""

        val fac = JsonNodeFactory.instance

        val jackson = doc.foldWith(Model.toJacksonFolder(fac, removeNullFields = true))
        assert(jackson.isObject)
        jackson.size() shouldEqual 3
        assert(jackson.get("good").isNumber)
        jackson.get("good").asInt() shouldEqual 3
        assert(!jackson.has("bad"))

        val arrNode = jackson.get("array")
        assert(arrNode.isArray)
        arrNode.size() shouldEqual 3
        arrNode.get(0).asInt shouldEqual 1
        assert(arrNode.get(1).isNull)
        arrNode.get(2).asInt shouldEqual 3
        val nestedNode = jackson.get("nested")
        assert(nestedNode.isObject)
        nestedNode.size() shouldEqual 1
        assert(nestedNode.get("good").isNumber)
        nestedNode.get("good").asInt shouldEqual 3
        assert(!nestedNode.has("bad"))
      }

    }

  }

}
