package com.github.cndjp.godfather.domain.user_elements

import org.jsoup.select.Elements

// HTMLのドキュメントからUserを抽出したエレメントを他のHTMLエレメントと区別するための値クラス
case class UserElements(elems: Elements) extends AnyVal
