package com.github.cndjp.godfather.infrastructure.repository.event

import java.net.URL

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.github.cndjp.godfather.support.GodfatherTestSupport
import org.jsoup.Jsoup

class ConnpassEventRepositorySpec extends GodfatherTestSupport {
  val mockScrapeAdapter: ScrapeAdapter = mock[ScrapeAdapter]
  val mockRepository = new ConnpassEventRepositoryImpl(mockScrapeAdapter)

  describe("#getEventTitle") {
    describe("指定の形式のHTMLが入力されると、") {
      it("想定通りのイベントタイトルが返って来ること") {
        (mockScrapeAdapter.getDocument _)
          .expects(*)
          .returning(IO(Right(Jsoup.parse(mockHTML))))
          .once()

        val maybeResult = mockRepository
          .getEventTitle(ConnpassEvent(new URL("https://cnd.connpass.com/event/dummy/")))
          .unsafeRunSync()

        maybeResult shouldBe "水の呼吸勉強会"
      }
    }
  }

  describe("#getParticipantElements") {
    describe("指定の形式のHTMLが入力されると、") {
      it("想定通りのHTMLエレメントとstatusが返って来ること") {}
    }
  }
}
