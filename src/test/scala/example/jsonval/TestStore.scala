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
