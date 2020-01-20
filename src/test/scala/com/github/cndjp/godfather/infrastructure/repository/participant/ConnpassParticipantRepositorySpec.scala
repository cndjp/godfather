package com.github.cndjp.godfather.infrastructure.repository.participant

import java.net.URL

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.github.cndjp.godfather.infrastructure.repository.event.ConnpassEventRepositoryImpl
import com.github.cndjp.godfather.support.GodfatherTestSupport
import org.jsoup.Jsoup

class ConnpassParticipantRepositorySpec extends GodfatherTestSupport {
  val mockEventScrapeAdapter: ScrapeAdapter = mock[ScrapeAdapter]
  val mockEventRepository = new ConnpassEventRepositoryImpl(mockEventScrapeAdapter)
  val mockParticipantScrapeAdapter: ScrapeAdapter = mock[ScrapeAdapter]

  val mockParticipantRepository = new ConnpassParticipantRepositoryImpl(
    mockParticipantScrapeAdapter)

  describe("#element2Participants") {
    describe("指定の形式のHTMLが入力されると、") {
      it("想定通りのイベントタイトルが返って来ること") {
        (mockEventScrapeAdapter.getDocument _)
          .expects(*)
          .returning(IO(Right(Jsoup.parse(mockHTML))))
          .once()

        val eventResult = mockEventRepository
          .getParticipantElements(ConnpassEvent(new URL("https://cnd.connpass.com/event/dummy/")))
          .unsafeRunSync()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/tanjiro/open/")
          .returning(IO(Right(Jsoup.parse("<h1>たんじろのページ</h1>"))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/zenitsu/open/")
          .returning(IO(Right(Jsoup.parse("<h1>ぜんいつのページ</h1>"))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/ponyo/")
          .returning(IO(Right(Jsoup.parse("<h1>ポニョのページ</h1>"))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/sousuke/")
          .returning(IO(Right(Jsoup.parse("<h1>宗介のページ</h1>"))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/kirby/")
          .returning(IO(Right(Jsoup.parse("<h1>カービィのページ</h1>"))))
          .once()

        val result = mockParticipantRepository.element2Participants(eventResult).unsafeRunSync()
        result(0).name shouldBe "tanjiro"
        result(0).name shouldBe "tanjiro"
        result(1).name shouldBe "zenitsu"
        result(2).name shouldBe "Ponyo"
        result(3).name shouldBe "Sousuke"
        result(4).name shouldBe "カービィ"
      }
    }
  }
}
