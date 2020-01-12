import sbt._

name := "godfather"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.jsoup" % "jsoup" % "1.12.1",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-free" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "com.vladkopanev" %% "cats-saga" % "0.2.3",
  "com.github.scopt" %% "scopt" % "4.0.0-RC2"
)