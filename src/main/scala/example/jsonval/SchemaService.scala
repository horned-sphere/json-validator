package example.jsonval

import cats.effect.IO
import example.jsonval.response.{SchemaResponse, ValidationResponse}
import io.circe.Json
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.io._

object SchemaService {

  /**
   * Create an HTTP service exposing the schema store and validator as a REST web service.
   * @param store A persistent store of JSON schemas.
   * @param validator Schema validator.
   * @return The service definition.
   */
  def service(store: SchemaStore, validator: Validator): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "schema" / name =>
      store.get(name).flatMap {
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
  }

  def uploadSchema(store: SchemaStore,
                   name: String,
                   schema: Json): IO[Response[IO]] = {
    store.insert(name, schema).flatMap {
      case Left(err) => InternalServerError(SchemaResponse.failedUpload(name, err))
      case _ => Created(SchemaResponse.goodUpload(name))
    }
  }

  def validateDocument(store: SchemaStore,
                       validator: Validator,
                       name: String,
                       document: Json): IO[Response[IO]] = {
    store.get(name).flatMap {
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
