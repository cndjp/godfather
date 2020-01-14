import sbt._

name := "godfather"

version := "0.1"

scalaVersion := "2.12.8"

val catsVersion = "2.0.0"
val circeVersion = "0.10.0"
val finagleVersion = "19.12.0"
val finchVersion = "0.31.0"
val macwireVersion = "2.3.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsVersion,
  "com.vladkopanev" %% "cats-saga" % "0.2.3",
  "com.github.finagle" %% "finchx-core" % finchVersion,
  "com.twitter" %% "finagle-core" % finagleVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "twitter-server" % finagleVersion,
  "com.twitter" %% "util-core" % finagleVersion,
  "com.github.finagle" %% "finchx-circe" % finchVersion,
  "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "com.softwaremill.macwire" %% "proxy" % macwireVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.github.pathikrit" %% "better-files" % "3.7.1",
  "com.beachape" %% "enumeratum" % "1.5.13"
)