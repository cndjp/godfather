package com.github.cndjp.godfather.infrastructure.adapter.scrape

import cats.effect.IO
import com.github.cndjp.godfather.exception.GodfatherException
import org.jsoup.nodes.Document

trait ScrapeAdapter {
  def getDocument(url: String): IO[Either[Throwable, Document]]
}
