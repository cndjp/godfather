package com.github.cndjp.godfather.domain.participant

import enumeratum._

sealed abstract class ParticipantStatus(id: Int, name: String) extends EnumEntry {
  def getName = this.name
}

case object ParticipantStatus extends Enum[ParticipantStatus] {
  case object ORGANIZER extends ParticipantStatus(1, "ORGANIZER")
  case object PARTICIPANT extends ParticipantStatus(2, "PARTICIPANT")
  case object WAITLISTED extends ParticipantStatus(3, "WAITLISTED")
  case object CANCELLED extends ParticipantStatus(4, "CANCELLED")

  val values = findValues
}
