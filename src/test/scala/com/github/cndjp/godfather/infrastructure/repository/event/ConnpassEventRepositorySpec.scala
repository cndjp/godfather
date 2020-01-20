package com.github.cndjp.godfather.infrastructure.repository.event

import java.net.URL

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.ParticipantStatus
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.github.cndjp.godfather.support.GodfatherTestSupport
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ConnpassEventRepositorySpec extends GodfatherTestSupport {
  val mockScrapeAdapter: ScrapeAdapter = mock[ScrapeAdapter]
  val mockRepository = new ConnpassEventRepositoryImpl(mockScrapeAdapter)

  describe("#getEventTitle") {
    describe("指定の形式のHTMLが入力されると、") {
      it("想定通りのイベントタイトルが返って来ること") {
        (mockScrapeAdapter.getDocument _)
          .expects(*)
          .returning(IO(Right(Jsoup.parse(mockConnpassHTML))))
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
      it("想定通りのHTMLエレメントとstatusが返って来ること") {
        (mockScrapeAdapter.getDocument _)
          .expects(*)
          .returning(IO(Right(Jsoup.parse(mockConnpassHTML))))
          .once()

        val actualResult = mockRepository
          .getParticipantElements(ConnpassEvent(new URL("https://cnd.connpass.com/event/dummy/")))
          .unsafeRunSync()

        val actualOrganizerResults = actualResult.head._2.toArray(Array[Element]())
        actualOrganizerResults.head.select("p.display_name a").text() shouldBe "tanjiro zenitsu"

        val actualParticipantResults = actualResult(1)._2.toArray(Array[Element]())
        actualParticipantResults.head.select("p.display_name a").text() shouldBe "Ponyo Sousuke"

        val actualWaitResults = actualResult(2)._2.toArray(Array[Element]())
        actualWaitResults.head.select("p.display_name a").text() shouldBe "カービィ"

        actualResult.foreach(_._1 should not be ParticipantStatus.CANCELLED)
      }
    }
  }
}
