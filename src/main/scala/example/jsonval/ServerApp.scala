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

import cats.effect._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import scopt.Read.reads
import scopt.{OParserBuilder, Read}

import java.nio.file.Path
import scala.concurrent.ExecutionContext.global

object ServerApp extends IOApp {

  implicit val shortRead: Read[Short] = reads { str =>
    java.lang.Short.parseShort(str)
  }

  import scopt.OParser

  private val builder: OParserBuilder[AppConfig] = OParser.builder[AppConfig]
  private val configParser: OParser[Unit, AppConfig] = {
    import builder._
    OParser.sequence(
      programName("json-validator"),
      head("json-validator", "0.1"),
      opt[Short]('p', "foo")
        .action((p, c) => c.copy(port = p))
        .required()
        .text("The port to bind to"),
      opt[String]('s', "storePath")
        .action((p, c) => c.copy(storePath = p))
        .required()
        .text("The port to bind to"),
    )
  }

  override def run(args: List[String]): IO[ExitCode] = {


    IO(OParser.parse(configParser, args, AppConfig())).flatMap {
      case Some(config) =>

        config.validate().flatMap {
          case Left(err) => IO(println(err)) *> IO.pure(ExitCode.Error)
          case Right((port, storePath)) =>
            val app = for {
              blocker <- Blocker[IO]
              store = Fs2SchemaStore.create(storePath, blocker)
              app = Service.service(store, Validator.default())
              server <- BlazeServerBuilder[IO](global)
                .bindHttp(port)
                .withHttpApp(Router("/" -> app).orNotFound)
                .resource
            } yield server

            app.use(_ => IO.never).as(ExitCode.Success)
        }
      case _ => IO.pure(ExitCode.Error)
    }

  }
}


final case class AppConfig(port: Short = 8080, storePath: String = "") {

  def validate(): IO[Either[String, (Short, Path)]] = if (port <= 0) {
    IO.pure(Left("Port must be positive."))
  } else {
    IO({
      val path = Path.of(storePath)
      (path, path.toFile)
    }).attempt.map {
      case Left(err) => Left(err.getMessage)
      case Right((path, file)) =>
        if (!file.exists() || !file.isDirectory) {
          Left(s"Store path $storePath is not a directory.")
        } else {
          Right((port, path))
        }
    }
  }

}
