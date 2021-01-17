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
