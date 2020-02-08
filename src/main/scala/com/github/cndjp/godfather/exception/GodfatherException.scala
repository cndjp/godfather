package com.github.cndjp.godfather.exception

sealed abstract class GodfatherException(why: String) extends Exception(why)

object GodfatherException {
  case class GodfatherGeneralException(why: String)
      extends GodfatherException(s"予測されないエラーが発生しました: $why")

  case class GodfatherRendererException(why: String)
      extends GodfatherException(s"レンダリングに失敗しました: $why")

  case class GodfatherScrapeException(why: String)
      extends GodfatherException(s"スクレイピングに失敗しました: $why")

  case class GodfatherParseArgsException(why: String)
      extends GodfatherException(s"引数のパースに失敗しました: $why")
}
