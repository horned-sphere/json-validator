package example.jsonval.response

import io.circe.Encoder
import io.circe.literal._

/**
 * Response type for the schema upload and retrieval endpoints.
 *
 * @param action The name of the action.
 * @param id The name of the schema in the store.
 * @param status Indicates the status of the request (success, failure, etc).
 * @param message Optional message giving more details on the status.
 */
final case class SchemaResponse(action: String, id: String, status: String, message: Option[String])

object SchemaResponse {

  def badUpload(name: String): SchemaResponse = SchemaResponse("uploadSchema", name, "error", Some("Invalid JSON"))

  def failedUpload(name: String, err: String): SchemaResponse = SchemaResponse("uploadSchema", name, "failed", Some(err))

  def goodUpload(name: String): SchemaResponse = SchemaResponse("uploadSchema", name, "success", None)

  def schemaNotFound(name: String): SchemaResponse = SchemaResponse("getSchema", name, "notFound", None)

  implicit val SchemaResponseEncoder: Encoder[SchemaResponse] = (response: SchemaResponse) => {
    response.message match {
      case Some(msg) =>
        json"""
                {
                    "action": ${response.action},
                    "id": ${response.id},
                    "status": ${response.status},
                    "message": $msg
                }
                """
      case _ =>
        json"""
                {
                    "action": ${response.action},
                    "id": ${response.id},
                    "status": ${response.status}
                }
                """

    }
  }

}
