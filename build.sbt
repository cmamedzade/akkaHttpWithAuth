ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "CashIn"
  )

val AkkaVersion = "2.6.14"
val AkkaHttpVersion = "10.2.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.4",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.github.bunyod" %% "authentikat-jwt" % "0.5.1",
  "org.postgresql" % "postgresql" % "42.3.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
  "org.scalactic" %% "scalactic" % "3.2.11",
  "org.scalatest" %% "scalatest" % "3.2.11" % "test",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.1.0" % Test)