package com.github.cndjp.godfather.infrastructure.repository.participant

import java.net.URL
import java.util.UUID

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.ConnpassParticipant
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
          .returning(IO(Right(Jsoup.parse(mockConnpassHTML))))
          .once()

        val eventResult = mockEventRepository
          .getParticipantElements(ConnpassEvent(new URL("https://cnd.connpass.com/event/dummy/")))
          .unsafeRunSync()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/tanjiro/open/")
          .returning(IO(Right(Jsoup.parse(
            """<div id="side_area"><div class="mb_20 text_center"><a class="image_link" href="https://connpass.com/user/dummy/tanjiro.png"><img src="https://connpass.com/user/dummy/tanjiro.png" width="180" height="180" title="tanjiro" alt="tanjiro"></a></div></div>"""))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/zenitsu/open/")
          .returning(IO(Right(Jsoup.parse(
            """<div id="side_area"><div class="mb_20 text_center"><a class="image_link" href="https://connpass.com/user/dummy/zenitsu.png"><img src="https://connpass.com/user/dummy/zenitsu.png" width="180" height="180" title="zenitsu" alt="zenitsu"></a></div>"""))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/ponyo/")
          .returning(IO(Right(Jsoup.parse(
            """<div id="side_area"><div class="mb_20 text_center"><a class="image_link" href="https://connpass.com/user/dummy/ponyo.png"><img src="https://connpass.com/user/dummy/ponyo.png" width="180" height="180" title="ponyo" alt="ponyo"></a></div>"""))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/sousuke/")
          .returning(IO(Right(Jsoup.parse(
            """<div id="side_area"><div class="mb_20 text_center"><a class="image_link" href="https://connpass.com/user/dummy/sousuke.png"><img src="https://connpass.com/user/dummy/sousuke.png" width="180" height="180" title="sousuke" alt="sousuke"></a></div>"""))))
          .once()

        (mockParticipantScrapeAdapter.getDocument _)
          .expects("https://connpass.com/user/kirby/")
          .returning(IO(Right(Jsoup.parse(
            """<div id="side_area"><div class="mb_20 text_center"><a class="image_link" href="https://connpass.com/user/dummy/kirby.png"><img src="https://connpass.com/user/dummy/kirby.png" width="180" height="180" title="kirby" alt="kirby"></a></div>"""))))
          .once()

        val result = mockParticipantRepository.element2Participants(eventResult).unsafeRunSync()
        result(0).name shouldBe "tanjiro"
        result(0).imageURL shouldBe new URL("https://connpass.com/user/dummy/tanjiro.png")
        result(1).name shouldBe "zenitsu"
        result(1).imageURL shouldBe new URL("https://connpass.com/user/dummy/zenitsu.png")
        result(2).name shouldBe "Ponyo"
        result(2).imageURL shouldBe new URL("https://connpass.com/user/dummy/ponyo.png")
        result(3).name shouldBe "Sousuke"
        result(3).imageURL shouldBe new URL("https://connpass.com/user/dummy/sousuke.png")
        result(4).name shouldBe "カービィ"
        result(4).imageURL shouldBe new URL("https://connpass.com/user/dummy/kirby.png")
      }
    }
  }

  describe("#parseParticipantList") {
    describe("指定のConnpassParticipantのリスト入れると") {
      it("想定通りのHTMLが返って来ること") {
        val actual = mockParticipantRepository
          .parseParticipantList(
            "水の呼吸勉強会",
            Seq(
              ConnpassParticipant(
                UUID.randomUUID().toString,
                "tanjiro",
                new URL("https://connpass.com/user/dummy/tanjiro.png")),
              ConnpassParticipant(
                UUID.randomUUID().toString,
                "zenitsu",
                new URL("https://connpass.com/user/dummy/zenitsu.png")),
              ConnpassParticipant(
                UUID.randomUUID().toString,
                "Ponyo",
                new URL("https://connpass.com/user/dummy/ponyo.png")),
              ConnpassParticipant(
                UUID.randomUUID().toString,
                "Sousuke",
                new URL("https://connpass.com/user/dummy/sousuke.png")),
              ConnpassParticipant(
                UUID.randomUUID().toString,
                "カービィ",
                new URL("https://connpass.com/user/dummy/kirby.png")),
            )
          )
          .unsafeRunSync()
          .doc

        Jsoup.parse(actual).outerHtml() shouldBe Jsoup.parse(mockCardsHTML).outerHtml()
      }
    }
  }
}
