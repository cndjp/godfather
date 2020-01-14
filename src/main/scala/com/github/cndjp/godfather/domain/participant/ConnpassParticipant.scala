package com.github.cndjp.godfather.domain.participant

import java.net.URL

case class ConnpassParticipant(id: String, name: String, imageURL: URL, status: ParticipantStatus)
    extends Participant