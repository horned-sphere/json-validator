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
import fs2.{Stream, text}
import io.circe.Json
import io.circe.fs2.byteStreamParser
import io.circe.literal._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{Method, Request, Uri, _}
import weaver.SimpleIOSuite

import scala.collection.mutable

object ServiceSpec extends SimpleIOSuite {

  def makeService(): HttpApp[IO] = {
    val app = SchemaService.service(TestStore(), Validator.default())
    Router("/" -> app).orNotFound
  }

  def makeService(name: String, schema: Json): HttpApp[IO] = {
    val map = mutable.Map(name -> schema)
    val app = SchemaService.service(new TestStore(map), Validator.default())
    Router("/" -> app).orNotFound
  }

  def makeRequest(method: Method, uri: Uri, body: Json): Request[IO] = {
    Request(method, uri, body = Stream.emit(body.toString()).through(text.utf8Encode))
  }

  def makeRequest(method: Method, uri: Uri): Request[IO] = {
    Request(method, uri)
  }

  def makeRequest(method: Method, uri: Uri, body: String): Request[IO] = {
    Request(method, uri, body = Stream.emit(body).through(text.utf8Encode))
  }

  def tryDecodeBody(response: Response[IO]): IO[Either[String, Json]] = {
    response.body
      .through(byteStreamParser)
      .attempt
      .compile
      .lastOrError
      .map(_.left.map(_.getMessage))
  }

  test("get non-existent schema") {
    val service = makeService()
    val request = makeRequest(Method.GET, uri"/schema/my-schema")
    for {
      response <- service.run(request)
    } yield expect(response.status == Status.NotFound)
  }

  test("get schema") {
    val service = makeService("my-schema", Examples.ValidSchema)
    val request = makeRequest(Method.GET, uri"/schema/my-schema")
    for {
      response <- service.run(request)
      decodedBody <- tryDecodeBody(response)
    } yield expect(response.status == Status.Ok) && expect(decodedBody == Right(Examples.ValidSchema))
  }

  test("upload good schema") {
    val service = makeService()
    val request = makeRequest(Method.POST, uri"/schema/my-schema", Examples.ValidSchema.toString)
    val expectedResponse =
      json"""
        {
          "action": "uploadSchema",
          "id": "my-schema",
          "status": "success"
        }
        """
    for {
      response <- service.run(request)
      decodedBody <- tryDecodeBody(response)
    } yield expect(response.status == Status.Created) && expect(decodedBody == Right(expectedResponse))
  }

  test("upload bad JSON") {
    val service = makeService()
    val request = makeRequest(Method.POST, uri"/schema/my-schema", "Nonsense")
    val expectedResponse =
      json"""
        {
          "action": "uploadSchema",
          "id": "my-schema",
          "status": "error",
          "message": "Invalid JSON"
        }
        """
    for {
      response <- service.run(request)
      decodedBody <- tryDecodeBody(response)
    } yield expect(response.status == Status.BadRequest) && expect(decodedBody == Right(expectedResponse))
  }

  test("validate good document") {
    val service = makeService("my-schema", Examples.ValidSchema)
    val request = makeRequest(Method.POST, uri"/validate/my-schema", Examples.GoodDocument.toString)
    val expectedResponse =
      json"""
        {
          "action": "validateDocument",
          "id": "my-schema",
          "status": "success"
        }
        """
    for {
      response <- service.run(request)
      decodedBody <- tryDecodeBody(response)
    } yield expect(response.status == Status.Ok) && expect(decodedBody == Right(expectedResponse))
  }

  test("validate bad schema") {
    val service = makeService("my-schema", Examples.ValidSchema)
    val request = makeRequest(Method.POST, uri"/validate/my-schema", Examples.BadDocument.toString)
    val expectedResponse =
      json"""
        {
          "action": "validateDocument",
          "id": "my-schema",
          "status": "error",
          "message": ["object has missing required properties ([\"destination\",\"source\"])"]
        }
        """
    for {
      response <- service.run(request)
      decodedBody <- tryDecodeBody(response)
    } yield expect(response.status == Status.BadRequest) && expect(decodedBody == Right(expectedResponse))
  }

}
