package example.jsonval

import cats.effect.{Blocker, IO, Resource}
import io.circe.Json
import weaver.IOSuite

import java.nio.file.{Files, Path}

object Fs2SchemaStoreSpec extends IOSuite {
  override type Res = Fs2SchemaStore

  def makeTemp(): IO[Path] = {
    IO(Files.createTempDirectory("store"))
  }

  override def sharedResource: Resource[IO, Fs2SchemaStore] = {
    for {
      storePath <- Resource.liftF(makeTemp())
      blocker <- Blocker[IO]
      store = Fs2SchemaStore.create(storePath, blocker)
    } yield store
  }

  val BadName ="schema1"

  test("get non-existent") { store =>
    for {
      result <- store.get(BadName)
    } yield expect(result match {
      case Right(None) => true
      case _ => false
    })
  }

  val GoodName = "schema2"

  test("insert schema") { store =>
    for {
      result <- store.insert(GoodName, Examples.ValidSchema)
    } yield expect(result == Right(()))
  }

  def insertAndGet(store: Fs2SchemaStore): IO[Either[String, Option[Json]]] = {
    store.insert(GoodName, Examples.ValidSchema).flatMap {
      case Left(err) => IO.pure(Left(err))
      case _ => store.get(GoodName)
    }
  }

  test("insert and get") { store =>
    for {
      insertResult <- insertAndGet(store)
    } yield {
      expect(insertResult == Right(Some(Examples.ValidSchema)))
    }
  }
}
