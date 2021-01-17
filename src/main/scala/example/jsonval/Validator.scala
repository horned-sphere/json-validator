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

/**
 * A validator that will attempt to validate JSON documents against a (potentially invalid) JSON schema.
 * @param nodeFactory Node factory for creating Jackson JSON nodes for the validator.
 * @param schemaFactory Factory to create schema instances from JSON schema documents.
 */
class Validator(nodeFactory: JsonNodeFactory, schemaFactory: JsonSchemaFactory) {

  private val syntaxValidator = schemaFactory.getSyntaxValidator

  /**
   * Attempt to validate the document against the schema.
   * @param schema JSON schema.
   * @param document JSON document.
   * @return The result can either be a success or an error indicating that either the schema was invalid or the
   *         document failed to satisfy it.
   */
  def validate(schema: Json, document: Json): Either[ValidationError, Unit] = {
    val jacksonSchema = schema.foldWith(Model.toJacksonFolder(nodeFactory, removeNullFields = false))
    val jacksonDocument = document.foldWith(Model.toJacksonFolder(nodeFactory, removeNullFields = true))
    Try {
      if (syntaxValidator.schemaIsValid(jacksonSchema)) {
        val validator = schemaFactory.getJsonSchema(jacksonSchema)
        val result = validator.validate(jacksonDocument)
        if (result.isSuccess) {
          Right(())
        } else {
          Left(ValidationFailed(result.asScala.map(_.getMessage).toList))
        }
      } else Left(InvalidSchema("Schema is not valid."))
    } match {
      case Success(result) => result
      case Failure(t) => Left(InvalidSchema(t.getMessage))
    }
  }

}

sealed trait ValidationError

/**
 * The schema provided was invalid.
 * @param message An error message from the validator exception.
 */
final case class InvalidSchema(message: String) extends ValidationError

/**
 * The document did not satisfy the schema.
 * @param errors The list of errors from the validator.
 */
final case class ValidationFailed(errors: List[String]) extends ValidationError

object Validator {

  def default(): Validator = {
    new Validator(JsonNodeFactory.instance, JsonSchemaFactory.byDefault())
  }

}