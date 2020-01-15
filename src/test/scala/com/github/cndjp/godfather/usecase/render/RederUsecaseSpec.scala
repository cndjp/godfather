package com.github.cndjp.godfather.usecase.render

import java.net.URL
import java.util.UUID

import cats.effect.IO
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.support.GodfatherTestSupport
import com.twitter.io.Buf
import org.jsoup.select.Elements

class RederUsecaseSpec extends GodfatherTestSupport {
  val mockConnpassEventRepository: ConnpassEventRepository = mock[ConnpassEventRepository]

  val mockConnpassParticipantRepository: ConnpassParticipantRepository =
    mock[ConnpassParticipantRepository]

  val mockUsecase = new RenderUsecaseImpl(
    mockConnpassEventRepository,
    mockConnpassParticipantRepository
  ) { override lazy val resourcesPath: String = "./src/test/resources" }

  describe("#exec") {
    describe("指定のエンドポイントを叩くと、") {
      it("OK が返ってくること") {
        val expectHTML = "<h1>ダミーのINDEXだよん</h1>"
        (mockConnpassEventRepository.getEventTitle _).expects(*).returning(IO("水の呼吸勉強会")).once()
        (mockConnpassEventRepository.getElements _)
          .expects(*)
          .returning(
            IO(
              Seq(
                (ParticipantStatus.ORGANIZER, new Elements()),
                (ParticipantStatus.PARTICIPANT, new Elements()),
                (ParticipantStatus.WAITLISTED, new Elements()),
              )))
          .once()
        (mockConnpassParticipantRepository
          .element2Participants(_: Seq[(ParticipantStatus, Elements)]))
          .expects(*)
          .returning(
            IO(
              Seq(
                ConnpassParticipant(
                  UUID.randomUUID().toString,
                  "まま",
                  new URL("http://exmple/image/1")))))
          .once()
        (mockConnpassParticipantRepository
          .parseParticipantList(_: String, _: Seq[ConnpassParticipant]))
          .expects(*, *)
          .returning(IO(expectHTML))
          .once()

        val actual = mockUsecase
          .exec(ConnpassEvent(new URL("https://cnd.connpass.com/event/154414/")))
          .unsafeRunSync()
        Buf.Utf8.unapply(actual).get shouldBe expectHTML
      }
    }
  }
}
