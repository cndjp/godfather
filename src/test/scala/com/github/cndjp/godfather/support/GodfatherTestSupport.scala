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

  lazy val cardsHTML: String = {
    val mockHTMLFile = Source.fromFile(s"$testResourcesPath/cards.html")
    try mockHTMLFile.mkString
    finally mockHTMLFile.close
  }

  lazy val mockCardsHTML: String = {
    val mockHTMLFile = Source.fromFile(s"$testResourcesPath/mock_cards.html")
    try mockHTMLFile.mkString
    finally mockHTMLFile.close
  }

  lazy val mockConnpassHTML: String = {
    val mockHTMLFile = Source.fromFile(s"$testResourcesPath/mock_connpass.html")
    try mockHTMLFile.mkString
    finally mockHTMLFile.close
  }
}
