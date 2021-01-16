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

package example.jsonval

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.Json

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class Validator(nodeFactory: JsonNodeFactory, schemaFactory: JsonSchemaFactory) {

  def validate(schema: Json, document: Json): Either[ValidationError, Unit] = {
    val jacksonSchema = schema.foldWith(Model.toJacksonFolder(nodeFactory, removeNullFields = false))
    val jacksonDocument = document.foldWith(Model.toJacksonFolder(nodeFactory, removeNullFields = true))
    Try {
      val validator = schemaFactory.getJsonSchema(jacksonSchema)
      val result = validator.validate(jacksonDocument)
      if (result.isSuccess) {
        Right(())
      } else {
        Left(ValidationFailed(result.asScala.map(_.getMessage).toList))
      }
    } match {
      case Success(result) => result
      case Failure(t) => Left(InvalidSchema(t.getMessage))
    }
  }

}

sealed trait ValidationError

final case class InvalidSchema(message: String) extends ValidationError
final case class ValidationFailed(errors: List[String]) extends ValidationError

object Validator {

  def default(): Validator = {
    new Validator(JsonNodeFactory.instance, JsonSchemaFactory.byDefault())
  }

}