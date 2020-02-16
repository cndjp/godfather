package com.github.cndjp.godfather.infrastructure.adapter.scrape

import java.io.IOException

import cats.effect.IO
import com.github.cndjp.godfather.domain.validUrl.ValidUrl
import com.github.cndjp.godfather.exception.GodfatherException
import com.github.cndjp.godfather.exception.GodfatherException.{
  GodfatherParseUrlException,
  GodfatherScrapeException
}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.Try

class ScrapeAdapterImpl extends ScrapeAdapter {
  override def getDocument(url: ValidUrl): IO[Either[Throwable, Document]] =
    IO(Try(Jsoup.connect(url.toString).get).toEither)
}
