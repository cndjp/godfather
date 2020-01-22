package com.github.cndjp.godfather.domain.event

import java.net.URL

// connpassのイベントを表す値クラス
case class ConnpassEvent(url: URL) extends AnyVal {

  def getParticipantsListUrl: String = {
    val input = this.url.toString
    val suffix = if (input.endsWith("/")) "participation/" else "/participation/";
    input + suffix
  }
}
