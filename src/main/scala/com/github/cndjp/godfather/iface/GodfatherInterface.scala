package com.github.cndjp.godfather.iface

import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.infrastructure.adapter.scrape.{ScrapeAdapter, ScrapeAdapterImpl}
import com.github.cndjp.godfather.infrastructure.repository.event.ConnpassEventRepositoryImpl
import com.github.cndjp.godfather.infrastructure.repository.participant.ConnpassParticipantRepositoryImpl
import com.github.cndjp.godfather.usecase.render.{RenderUsecase, RenderUsecaseImpl}
import com.softwaremill.macwire.wire

trait GodfatherInterface {
  lazy val scrapeAdapter: ScrapeAdapter = wire[ScrapeAdapterImpl]
  lazy val renderUsecase: RenderUsecase = wire[RenderUsecaseImpl]
  lazy val connpassEventRepository: ConnpassEventRepository = wire[ConnpassEventRepositoryImpl]
  lazy val connpassParticipantRepository: ConnpassParticipantRepository =
    wire[ConnpassParticipantRepositoryImpl]
}
