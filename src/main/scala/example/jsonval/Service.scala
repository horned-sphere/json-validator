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

import cats.effect.IO
import example.jsonval.response.{SchemaResponse, ValidationResponse}
import io.circe.Json
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._

object Service {

  def service(store: SchemaStore, validator: Validator): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "schema" / name =>
      store.get(name).compile.lastOrError.flatMap {
        case Right(Some(json)) => Ok(json)
        case _ => NotFound(SchemaResponse.schemaNotFound(name))
      }
    case request @ POST -> Root / "schema" / name =>
      request.attemptAs[Json].fold(
        _ => BadRequest(SchemaResponse.badUpload(name)),
        schema => uploadSchema(store, name, schema)
      ).flatMap(identity)
    case request @ POST -> Root / "validate" / name =>
      request.attemptAs[Json].fold(
        _ => BadRequest(ValidationResponse.invalidJson(name)),
        document => validateDocument(store, validator, name, document)
      ).flatMap(identity)
      IO(Response(Status.Ok))
  }

  def uploadSchema(store: SchemaStore,
                   name: String,
                   schema: Json): IO[Response[IO]] = {
    store.insert(name, schema).compile.lastOrError.flatMap {
      case Left(err) => InternalServerError(SchemaResponse.failedUpload(name, err))
      case _ => Created(SchemaResponse.goodUpload(name))
    }
  }

  def validateDocument(store: SchemaStore,
                       validator: Validator,
                       name: String,
                       document: Json):  IO[Response[IO]] = {
    store.get(name).compile.lastOrError.flatMap {
      case Right(Some(schema)) =>
        validator.validate(schema, document) match {
          case Left(InvalidSchema(message)) => InternalServerError(ValidationResponse.badSchema(name, message))
          case Left(ValidationFailed(messages)) => BadRequest(ValidationResponse.failed(name, messages))
          case _ => Ok(ValidationResponse.valid(name))
        }
      case _ => NotFound(SchemaResponse.schemaNotFound(name))
    }
  }

}
