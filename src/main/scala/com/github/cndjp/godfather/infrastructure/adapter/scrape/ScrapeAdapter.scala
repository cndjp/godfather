package com.github.cndjp.godfather.infrastructure.adapter.scrape

import cats.effect.IO
import com.github.cndjp.godfather.domain.validUrl.ValidUrl
import org.jsoup.nodes.Document

trait ScrapeAdapter {
  def getDocument(arg: ValidUrl): IO[Either[Throwable, Document]]
}
