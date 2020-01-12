package com.github.cndjp.godfather.exception

sealed abstract class GodfatherException(why: String) extends Exception(why)

object GodfatherException {
  case class GeneralGodfatherException(why: String)
    extends GodfatherException(s"予測されないエラーが発生しました: $why")

  case class GodfatherEventException(why: String)
    extends GodfatherException(s"不正なイベントURLが検出されました: $why")

  case class GodfatherPreviewException(why: String)
    extends GodfatherException(s"プレビューサーバの処理で不正イベントを検知しました: $why")

  case class GodfatherRendererException(why: String)
    extends GodfatherException(s"レンダリングに失敗しました: $why")
}