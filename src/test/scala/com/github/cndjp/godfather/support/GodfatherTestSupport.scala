package com.github.cndjp.godfather.support

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Inside, Inspectors, Matchers, OptionValues}

trait GodfatherTestSupport
    extends FunSpec
    with Matchers
    with Inside
    with Inspectors
    with OptionValues
    with MockFactory {}
