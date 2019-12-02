lazy val root = (project in file(".")).settings(
  organization := "com.github.cndjp",
  name := "godfather",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.13.1",
  libraryDependencies += "org.jsoup" % "jsoup" % "1.12.1",
  libraryDependencies += "info.picocli" % "picocli" % "4.1.1"
)
