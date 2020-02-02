package com.github.cndjp.godfather.domain.repository.participant

import cats.effect.IO
import com.github.cndjp.godfather.domain.cards.RenderedCards
import com.github.cndjp.godfather.domain.elements.participants.ParticipantsElements
import com.github.cndjp.godfather.domain.event.ConnpassTitle
import com.github.cndjp.godfather.domain.participant.ConnpassParticipant

trait ConnpassParticipantRepository {

  def renderParticipantList(title: ConnpassTitle,
                            input: Seq[ConnpassParticipant]): IO[RenderedCards]

  def element2Participant(input: ParticipantsElements): IO[Seq[ConnpassParticipant]]
}
