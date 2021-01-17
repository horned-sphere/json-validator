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

/**
 * Trait for stores of schemas to back up the service.
 */
trait SchemaStore {

  /**
   * Attempt to get the schema with the provided key.
   * @param name The key.
   * @return The schema, if it exists or an error for an invalid path.
   */
  def get(name: String): IO[Either[String, Option[Json]]]

  /**
   * Attempt to store a schema, overwriting any existing schema with the same key.
   * @param name The name of the schema.
   * @param schema The schema document.
   * @return An error if writing to the store failed or the schema name was invalid.
   */
  def insert(name: String, schema: Json): IO[Either[String, Unit]]

}
