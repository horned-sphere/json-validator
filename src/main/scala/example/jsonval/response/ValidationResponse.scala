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
 * Response type for the schema validation endpoint.
 *
 * @param action The name of the action.
 * @param id The name of the schema in the store.
 * @param status Indicates the status of the request (success, failure, etc).
 * @param message Optional message giving more details on the status.
 */
final case class ValidationResponse(action: String, id: String, status: String, message: Option[List[String]])

object ValidationResponse {

  def valid(name: String): ValidationResponse = ValidationResponse("validateDocument", name, "success", None)

  def invalidJson(name: String): ValidationResponse = ValidationResponse("validateDocument", name, "error", Some(List("Invalid JSON")))

  def badSchema(name: String, message: String): ValidationResponse = ValidationResponse("validateDocument", name, "invalidSchema", Some(List(message)))

  def failed(name: String, messages: List[String]): ValidationResponse = ValidationResponse("validateDocument", name, "error", Some(messages))

  implicit val ValidationResponseEncoder: Encoder[ValidationResponse] = (response: ValidationResponse) => {
    response.message match {
      case Some(msgs) =>
        json"""
                {
                    "action": ${response.action},
                    "id": ${response.id},
                    "status": ${response.status},
                    "message": $msgs
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
