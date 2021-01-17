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

import blobstore.{Store, Path}
import blobstore.fs.FileStore
import cats.effect.{Blocker, Concurrent, ContextShift, IO}
import io.circe.Json
import io.circe.fs2._
import fs2.{Stream, text}

import java.nio.file.{Path => NioPath}

/**
 * A basic key-value store for JSON documents, backed by an FS2 blob store.
 * @param store The blob store implementation.
 */
class Fs2SchemaStore private(store: Store[IO]) extends SchemaStore {

  def get(name: String): IO[Either[String, Option[Json]]] = {
    val str = Path.fromString(name) match {
      case Some(path) =>
        store.get(path, Fs2SchemaStore.DefaultChunkSize)
          .through(byteStreamParser)
          .attempt
          .map(_.toOption)
          .map(Right(_))
      case _ => Stream.emit(Left(s"Invalid schema name: $name"))
    }
    str.compile.lastOrError
  }

  def insert(name: String, schema: Json): IO[Either[String, Unit]] = {
    val str = Path.fromString(name) match {
      case Some(path) =>
        Stream.emit(schema.toString())
          .through(text.utf8Encode)
          .through(store.put(path, overwrite = true))
          .attempt
          .map(_.left.map(_.getMessage))
      case _ => Stream.emit(Left(s"Invalid schema name: $name"))
    }
    str.compile.last.map(_.getOrElse(Right(())))
  }

}

object Fs2SchemaStore {

  private val DefaultChunkSize = 1024

  /**
   * Create a schema store backed by a directory in the local filesystem.
   * @param path The path in the file system.
   * @param blocker Cats effects blocker.
   * @return The store.
   */
  def create(path: NioPath, blocker: Blocker)(implicit F: Concurrent[IO], CS: ContextShift[IO]): Fs2SchemaStore = {
    new Fs2SchemaStore(FileStore(path, blocker))
  }

}

