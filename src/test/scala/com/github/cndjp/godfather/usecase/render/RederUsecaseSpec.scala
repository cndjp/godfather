package com.github.cndjp.godfather.usecase.render

import java.net.URL
import java.util.UUID

import cats.effect.IO
import com.github.cndjp.godfather.domain.cards.RenderedCards
import com.github.cndjp.godfather.domain.elements.participants.ParticipantsElements
import com.github.cndjp.godfather.domain.event.{ConnpassEvent, ConnpassTitle}
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.support.GodfatherTestSupport
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class RederUsecaseSpec extends GodfatherTestSupport {
  val mockConnpassEventRepository: ConnpassEventRepository = mock[ConnpassEventRepository]

  val mockConnpassParticipantRepository: ConnpassParticipantRepository =
    mock[ConnpassParticipantRepository]

  val mockUsecase = new RenderUsecaseImpl(
    mockConnpassEventRepository,
    mockConnpassParticipantRepository
  )

  describe("#exec") {
    describe("実行すると") {
      it("エラーなく終了出来ること") {
        import com.github.cndjp.godfather.utils.ResourcesImplicits.testResourcesPath._

        val dummyCardHTML = RenderedCards("<h1>ダミーのカードだよん</h1>")
        (mockConnpassEventRepository.getEventTitle _)
          .expects(*)
          .returning(IO(ConnpassTitle("水の呼吸勉強会")))
          .once()
        (mockConnpassEventRepository.getParticipantElements _)
          .expects(*)
          .returning(IO(Map(
            ParticipantStatus.ORGANIZER -> ParticipantsElements(new Elements()),
            ParticipantStatus.PARTICIPANT -> ParticipantsElements(new Elements()),
            ParticipantStatus.WAITLISTED -> ParticipantsElements(new Elements()),
          )))
          .once()
        (mockConnpassParticipantRepository
          .element2Participant(_: ParticipantsElements))
          .expects(*)
          .returning(
            IO(
              Seq(
                ConnpassParticipant(
                  UUID.randomUUID().toString,
                  "まま",
                  new URL("http://exmple/image/1")))))
          .repeat(3)
        (mockConnpassParticipantRepository
          .renderParticipantList(_: ConnpassTitle, _: Seq[ConnpassParticipant]))
          .expects(*, *)
          .returning(IO(dummyCardHTML))
          .once()

        mockUsecase
          .exec(ConnpassEvent(new URL("https://cnd.connpass.com/event/dummy/")))
          .unsafeRunSync()
        Jsoup.parse(cardsHTML).outerHtml() shouldBe Jsoup.parse(dummyCardHTML.doc).outerHtml()
      }
    }
  }
}
