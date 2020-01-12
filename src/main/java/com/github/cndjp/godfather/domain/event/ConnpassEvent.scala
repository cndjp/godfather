package com.github.cndjp.godfather.domain.event

import java.net.URL

case class ConnpassEvent(url: URL) extends Event {
  def getParticipantsListUrl: String = {
    val input = this.url.toString
    val suffix = if (input.endsWith("/")) "participation/" else  "/participation/";
    input + suffix
  }
}