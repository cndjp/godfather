package com.github.cndjp.godfather.domain.event

import java.net.URL

import com.github.cndjp.godfather.domain.validUrl.ValidUrl

// connpassのイベントを表す値クラス
case class ConnpassEvent(validUrl: ValidUrl) {

  def getParticipantsListUrl: ValidUrl = {
    this.validUrl.url.fold(
      e => ValidUrl(Left(e)),
      ok => {
        val input = ok.toString
        val suffix = if (input.endsWith("/")) "participation/" else "/participation/";
        ValidUrl(input + suffix)
      }
    )
  }
}
