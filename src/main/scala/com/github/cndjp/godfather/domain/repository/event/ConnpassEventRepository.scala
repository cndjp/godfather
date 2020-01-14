package com.github.cndjp.godfather.domain.repository.event

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import org.jsoup.select.Elements

trait ConnpassEventRepository {
  def getEventTitle(event: ConnpassEvent): IO[String]

  def getElements(event: ConnpassEvent): IO[Seq[(ParticipantStatus, Elements)]]
}
