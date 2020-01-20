package com.github.cndjp.godfather.domain.repository.participant

import cats.effect.IO
import com.github.cndjp.godfather.domain.cards.Cards
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import org.jsoup.select.Elements

trait ConnpassParticipantRepository {
  def parseParticipantList(title: String, input: Seq[ConnpassParticipant]): IO[Cards]
  def element2Participants(input: Seq[(ParticipantStatus, Elements)]): IO[Seq[ConnpassParticipant]]
}
