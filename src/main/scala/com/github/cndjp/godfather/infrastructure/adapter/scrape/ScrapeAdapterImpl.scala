package com.github.cndjp.godfather.infrastructure.adapter.scrape

import java.io.IOException

import cats.effect.IO
import com.github.cndjp.godfather.domain.adapter.scrape.ScrapeAdapter
import org.jsoup.nodes.Document

class ScrapeAdapterImpl extends ScrapeAdapter {
  override def getDocument(url: String): IO[Either[IOException, Document]] = ???
}
