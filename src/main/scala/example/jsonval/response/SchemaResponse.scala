// Copyright 2015-2020 SWIM.AI inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
