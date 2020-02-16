package com.github.cndjp.godfather.domain.validUrl

import java.net.URL
import scala.util.Try

// 文字列からURLの変換を安全に取り出す値クラスです
case class ValidUrl(url: Either[Throwable, URL]) extends AnyVal

object ValidUrl {
  def apply(maybeURL: String): ValidUrl = ValidUrl(Try(new URL(maybeURL)).toEither)
}
