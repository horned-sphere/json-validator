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
    } yield expect(insertResult match {
      case Right(Some(doc)) => doc == Examples.GoodDocument
      case _ => false
    })
  }
}
