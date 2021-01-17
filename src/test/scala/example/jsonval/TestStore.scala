package example.jsonval

import cats.effect.IO
import io.circe.Json

import scala.collection.mutable

// Dummy schema repository backed by a mutable map.
class TestStore(schemas: mutable.Map[String, Json]) extends SchemaStore {

  override def get(name: String): IO[Either[String, Option[Json]]] = IO {
    Right(schemas.get(name))
  }

  override def insert(name: String, schema: Json): IO[Either[String, Unit]] = IO {
    schemas.put(name, schema)
    Right(())
  }
}

object TestStore {

  def apply(): TestStore = new TestStore(mutable.Map.empty)

}
