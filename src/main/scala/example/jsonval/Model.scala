package example.jsonval

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.circe.Json.Folder
import io.circe.{Json, JsonNumber, JsonObject}

object Model {

  /**
   * Convert between the Circe JSON model and the Jackson model used by the Java schema validator library.
   * @param factory Jackson node factory.
   * @param removeNullFields If true, all fields with null nodes will be stripped from the output.
   * @return A Circe model folder that will generate a Jackson [[JsonNode]].
   */
  def toJacksonFolder(factory: JsonNodeFactory, removeNullFields: Boolean): Folder[JsonNode] = new Folder[JsonNode] {
    override def onNull: JsonNode = factory.nullNode()

    override def onBoolean(value: Boolean): JsonNode = factory.booleanNode(value)

    override def onNumber(value: JsonNumber): JsonNode = {
      value.toBigDecimal match {
        case Some(x) if x.isValidShort => factory.numberNode(x.toShort)
        case Some(x) if x.isValidInt => factory.numberNode(x.toInt)
        case Some(x) if x.isValidLong => factory.numberNode(x.toLong)
        case Some(x) if x.isExactFloat => factory.numberNode(x.toFloat)
        case Some(x) if x.isExactDouble => factory.numberNode(x.toDouble)
        case Some(x) => factory.numberNode(x.bigDecimal)
        case _ => factory.numberNode(value.toDouble)
      }
    }

    override def onString(value: String): JsonNode = factory.textNode(value)

    override def onArray(value: Vector[Json]): JsonNode = {
      val arrNode = factory.arrayNode(value.length)
      value.map(node => node.foldWith(this)).foreach(arrNode.add)
      arrNode
    }

    override def onObject(obj: JsonObject): JsonNode = {
      val fields = obj.toIterable.iterator
      val it = if (removeNullFields) {
        fields.filter {
          case (_, value) => !value.isNull
        }
      } else fields
      val node = factory.objectNode()
      it.foreach {
        case (name, value) => node.replace(name, value.foldWith(this))
      }
      node
    }
  }

}
