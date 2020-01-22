package com.github.cndjp.godfather.domain.repository.event

import cats.effect.IO
import com.github.cndjp.godfather.domain.elements.participants.ParticipantsElements
import com.github.cndjp.godfather.domain.event.{ConnpassEvent, ConnpassTitle}
import com.github.cndjp.godfather.domain.participant.ParticipantStatus
import org.jsoup.select.Elements

trait ConnpassEventRepository {
  def getEventTitle(event: ConnpassEvent): IO[ConnpassTitle]

  def getParticipantElements(event: ConnpassEvent): IO[Map[ParticipantStatus, ParticipantsElements]]
}
