package com.github.cndjp.godfather.endpoint.interface

import java.net.URL

import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository
import com.github.cndjp.godfather.infrastructure.repository.connpass_event.ConnpassEventRepositoryImpl
import com.github.cndjp.godfather.usecase.{RenderUsecase, RenderUsecaseImpl}
import com.softwaremill.macwire._

trait GodfatherInterface {
  lazy val renderUsecase: RenderUsecase = wire[RenderUsecaseImpl]
  lazy val connpassEventRepository: ConnpassEventRepository = wire[ConnpassEventRepositoryImpl]
}
