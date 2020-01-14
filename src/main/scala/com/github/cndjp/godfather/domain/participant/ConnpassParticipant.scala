package com.github.cndjp.godfather.domain.participant

import java.net.URL

// connpassイベントに参加する人を表すドメインクラス
case class ConnpassParticipant(id: String, name: String, imageURL: URL, status: ParticipantStatus)
    extends Participant
