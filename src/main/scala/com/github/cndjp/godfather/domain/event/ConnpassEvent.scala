package com.github.cndjp.godfather.domain.event

import java.net.URL

import com.github.cndjp.godfather.domain.validUrl.ValidUrl

// connpassのイベントを表す値クラス
case class ConnpassEvent(validUrl: ValidUrl) {

  def getParticipantsListUrl: Either[Throwable, ValidUrl] = {
    val input = validUrl.toString
    val suffix = if (input.endsWith("/")) "participation/" else "/participation/";
    ValidUrl.from(input + suffix)
  }
}
