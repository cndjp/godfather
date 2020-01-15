package com.github.cndjp.godfather.usecase.render

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.support.GodfatherTestSupport
import com.twitter.io.Buf

class RederUsecaseSpec extends GodfatherTestSupport {
  val mockConnpassEventRepository: ConnpassEventRepository = mock[ConnpassEventRepository]

  val mockConnpassParticipantRepository: ConnpassParticipantRepository =
    mock[ConnpassParticipantRepository]

  val mockUsecase = new RenderUsecaseImpl(
    mockConnpassEventRepository,
    mockConnpassParticipantRepository,
  )

  describe("#exec") {
    describe("指定のエンドポイントを叩くと、") {
      it("OK が返ってくること") {}
    }
  }
}
