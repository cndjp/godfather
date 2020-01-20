package com.github.cndjp.godfather.support

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Inside, Inspectors, Matchers, OptionValues}

import scala.io.Source

trait GodfatherTestSupport
    extends FunSpec
    with Matchers
    with Inside
    with Inspectors
    with OptionValues
    with MockFactory {
  val testResourcesPath: String = "./src/test/resources"

  val mockHTML: String = {
    val mockHTMLFile = Source.fromFile(s"$testResourcesPath/mock_connpass.html")
    try mockHTMLFile.mkString
    finally mockHTMLFile.close
  }
}
