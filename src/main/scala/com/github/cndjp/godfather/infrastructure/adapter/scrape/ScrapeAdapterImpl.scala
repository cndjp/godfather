package com.github.cndjp.godfather.infrastructure.adapter.scrape

import java.io.IOException

import cats.effect.IO
import com.github.cndjp.godfather.exception.GodfatherException
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherScrapeException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.Try

class ScrapeAdapterImpl extends ScrapeAdapter {
  override def getDocument(url: String): IO[Either[Throwable, Document]] =
    IO(Try(Jsoup.connect(url).get()).toEither)
}
