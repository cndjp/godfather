package com.github.cndjp.godfather.endpoint.interface

import java.net.URL

import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.infrastructure.repository.event.ConnpassEventRepositoryImpl
import com.github.cndjp.godfather.infrastructure.repository.participant.ConnpassParticipantRepositoryImpl
import com.github.cndjp.godfather.usecase.render.{RenderUsecase, RenderUsecaseImpl}
import com.softwaremill.macwire._

trait GodfatherInterface {
  lazy val renderUsecase: RenderUsecase = wire[RenderUsecaseImpl]
  lazy val connpassEventRepository: ConnpassEventRepository = wire[ConnpassEventRepositoryImpl]
  lazy val connpassParticipantRepository: ConnpassParticipantRepository =
    wire[ConnpassParticipantRepositoryImpl]
}
