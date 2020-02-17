package com.github.cndjp.godfather.domain.validUrl

import java.net.URL
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherParseUrlException
import scala.util.Try

// 文字列からURLの変換を安全に取り出す値クラスです
case class ValidUrl(protected val url: URL) {
  override def toString: String = this.url.toString
}

object ValidUrl {

  def from(maybeURL: String): Either[Throwable, ValidUrl] =
    Try(new URL(maybeURL)).toEither match {
      case Left(e)      => Left(GodfatherParseUrlException(e.getMessage))
      case Right(value) => Right(ValidUrl(value))
    }
}
