package com.github.cndjp.godfather.infrastructure.adapter.scrape

import java.io.IOException

import cats.effect.IO
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ScrapeAdapterImpl extends ScrapeAdapter {
  override def getDocument(url: String): IO[Either[IOException, Document]] =
    try IO(Right(Jsoup.connect(url).get()))
    catch { case e: IOException => IO(Left(e)) }
}
