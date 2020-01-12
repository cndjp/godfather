package com.github.cndjp.godfather.domain.participant

import enumeratum._

sealed abstract class ParticipantStatus(id: Int) extends EnumEntry

case object ParticipantStatus extends Enum[ParticipantStatus] {
  case object ORGANIZER extends ParticipantStatus(1)
  case object PARTICIPANT extends ParticipantStatus(2)
  case object WAITLISTED extends ParticipantStatus(3)
  case object CANCELLED extends ParticipantStatus(4)

  val values = findValues
}
