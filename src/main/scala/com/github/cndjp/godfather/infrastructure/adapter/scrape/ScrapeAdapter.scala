package com.github.cndjp.godfather.infrastructure.adapter.scrape

import java.io.IOException

import cats.effect.IO
import org.jsoup.nodes.Document

trait ScrapeAdapter {
  def getDocument(url: String): IO[Either[IOException, Document]]
}
