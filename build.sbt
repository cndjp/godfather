import sbt._

lazy val commonSettings = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
    case "application.conf"                            => MergeStrategy.concat
    case "unwanted.txt"                                => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
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
    "com.beachape" %% "enumeratum" % "1.5.13",
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5",
    "org.scalacheck" %% "scalacheck" % "1.14.0",
    "org.mockito" % "mockito-core" % "2.7.22",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0",
    "com.github.scopt" %% "scopt" % "3.7.1",
  ),
  test in assembly := {}
)

lazy val catsVersion = "2.0.0"
lazy val circeVersion = "0.10.0"
lazy val finagleVersion = "20.1.0"
lazy val finchVersion = "0.31.0"
lazy val macwireVersion = "2.3.1"

lazy val godfather = (project in file("."))
  .settings(
    commonSettings,
    inThisBuild(
      List(
        name := "godfather",
        version := "0.1-SNAPSHOT",
        assemblyJarName := "godfather.jar",
        organization := "com.github.cndjp",
        scalaVersion := "2.12.8",
        scalacOptions := Seq(
          "-deprecation",
          "-feature",
          "-unchecked",
          "-Ypartial-unification",
          "-Xfatal-warnings",
          "-language:higherKinds",
          "-target:jvm-1.8"
        )
      )
    )
  )