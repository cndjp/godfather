package com.github.cndjp.godfather.domain.participant

import java.net.URL

case class ConnpassParticipant(id: Long, name: String, imageURL: URL, status: ParticipantStatus) extends Participant
