name := "csv-parser"

version := "0.1"

scalaVersion := "2.12.10"

resolvers += ("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)

libraryDependencies ++= {
  object Version {
    val scalaTest          = "3.1.0"
    val mockitoScala       = "1.10.0"
    val akkaStream         = "2.6.1"
  }

  Seq(
    "com.typesafe.akka"       %% "akka-stream"             % Version.akkaStream,
    "com.typesafe.akka"       %% "akka-stream-testkit"     % Version.akkaStream % Test,
    "org.mockito"             %% "mockito-scala"           % Version.mockitoScala % Test,
    "org.scalatest"           %% "scalatest"               % Version.scalaTest % Test
  )
}

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-Ypartial-unification",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xfatal-warnings",
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

coverageEnabled := true
scalafmtOnCompile := true
parallelExecution in Test := true