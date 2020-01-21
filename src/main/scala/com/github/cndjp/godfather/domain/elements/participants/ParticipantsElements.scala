package com.github.cndjp.godfather.domain.elements.participants

import org.jsoup.select.Elements

// HTMLのドキュメントからUserを抽出したエレメントを他のHTMLエレメントと区別するための値クラス
case class ParticipantsElements(elems: Elements) extends AnyVal
