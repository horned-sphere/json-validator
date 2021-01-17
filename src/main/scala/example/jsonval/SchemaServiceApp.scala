package example.jsonval

import cats.effect._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import scopt.Read.reads
import scopt.{OParserBuilder, Read}

import java.nio.file.Path
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success, Try}

/**
 * Basic application to start a web server hosting the schema service.
 */
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
      opt[Short]('p', "port")
        .action((p, c) => c.copy(port = p))
        .required()
        .text("The port to bind to.")
        .validate(p => if (p > 0) success else failure("Port must be positive.")),
      opt[String]('s', "storePath")
        .action((p, c) => c.copy(storePath = p))
        .required()
        .validate(p => {
          Try {
            val path = Path.of(p)
            val file = path.toFile
            if (file.exists() && file.isDirectory)
              success
            else
              failure("The store path must exist and be a directory.")
          } match {
            case Success(result) => result
            case Failure(t) =>failure(s"Error accessing store path: ${t.getMessage}")
          }
        })
        .text("Filesystem path for the schema store. This should exist and be (initially) empty an writeable."),
    )
  }

  override def run(args: List[String]): IO[ExitCode] = {

    IO(OParser.parse(configParser, args, AppConfig())).flatMap {
      case Some(config) =>
        val storePath = Path.of(config.storePath)
        val app = for {
          blocker <- Blocker[IO]
          store = Fs2SchemaStore.create(storePath, blocker)
          app = SchemaService.service(store, Validator.default())
          server <- BlazeServerBuilder[IO](global)
            .bindHttp(config.port)
            .withHttpApp(Router("/" -> app).orNotFound)
            .resource
        } yield server

        app.use(_ => IO.never).as(ExitCode.Success)
      case _ => IO.pure(ExitCode.Error)
    }

  }
}

/**
 * Configuration type to hold the arguments to the application.
 * @param port The port to bind the server to.
 * @param storePath The file system path for the schema store. This should exist, be initially empty and be writeable.
 */
final case class AppConfig(port: Short = 8080, storePath: String = "")
