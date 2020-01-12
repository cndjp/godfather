package com.github.cndjp.godfather.endpoint.interface

import com.github.cndjp.godfather.usecase.{RenderUsecase, RenderUsecaseImpl}
import com.softwaremill.macwire._

trait GodfatherInterface {
  lazy val renderUsecase: RenderUsecase = wire[RenderUsecaseImpl]
}
