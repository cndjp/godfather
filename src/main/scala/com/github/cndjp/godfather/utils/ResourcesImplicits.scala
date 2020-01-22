package com.github.cndjp.godfather.utils

object ResourcesImplicits {

  object mainResourcesPath {
    implicit val resourcesPath: String = "./src/main/resources"
  }

  object testResourcesPath {
    implicit val resourcesPath: String = "./src/test/resources"
  }
}
