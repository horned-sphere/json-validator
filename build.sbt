name := "json-val"

version := "0.1"

scalaVersion := "2.13.4"

val Http4sVersion = "0.21.15"
val CirceVersion = "0.13.0"
val JsonSchemaValidatorVersion = "2.2.14"
val Fs2BlobstoreVersion = "0.7.3"
val ScoptVersion = "4.0.0"
val ScalatestVersion = "3.0.8"

libraryDependencies ++= Seq (
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
  "io.circe"        %% "circe-fs2"       % CirceVersion,
  "io.circe"        %% "circe-literal"       % CirceVersion % "test",
  "io.circe"        %% "circe-parser"       % CirceVersion % "test",
  "com.github.java-json-tools" % "json-schema-validator" % JsonSchemaValidatorVersion,
  "com.github.fs2-blobstore" %% "core"  % Fs2BlobstoreVersion,
  "com.github.scopt" %% "scopt" % ScoptVersion,
  "org.scalatest" %% "scalatest" % ScalatestVersion % "test"
)

