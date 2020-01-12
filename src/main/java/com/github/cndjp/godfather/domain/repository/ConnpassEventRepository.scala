package com.github.cndjp.godfather.domain.repository

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.ConnpassParticipant

trait ConnpassEventRepository {
  def getEventTitle(event: ConnpassEvent): IO[String]
  def getParticipants(event: ConnpassEvent): IO[Seq[ConnpassParticipant]]
}
