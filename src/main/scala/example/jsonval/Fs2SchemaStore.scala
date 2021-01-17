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

