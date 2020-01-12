import sbt._

name := "godfather"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-free" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "com.vladkopanev" %% "cats-saga" % "0.2.3",
  "com.github.scopt" %% "scopt" % "4.0.0-RC2",
  "com.github.finagle" %% "finchx-core" % "0.29.0",
  "com.twitter" %% "finagle-core" % "19.5.1",
  "com.twitter" %% "finagle-http" % "19.5.1",
  "com.twitter" %% "util-core" % "19.5.1",
  "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % "2.3.1" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.3.1",
  "com.softwaremill.macwire" %% "proxy" % "2.3.1",
  "com.github.pathikrit" %% "better-files" % "3.7.1",
  "com.beachape" %% "enumeratum" % "1.5.13"
)