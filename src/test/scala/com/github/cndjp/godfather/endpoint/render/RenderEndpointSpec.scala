package com.github.cndjp.godfather.endpoint

import java.net.URL

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.endpoint.render.RenderEndpoint
import com.github.cndjp.godfather.support.GodfatherTestSupport
import com.github.cndjp.godfather.usecase.render.RenderUsecase
import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import io.finch.Input

class RenderEndpointSpec extends GodfatherTestSupport {
  val mockRenderUsecase: RenderUsecase = mock[RenderUsecase]

  val mockEndpoint = new RenderEndpoint {
    override lazy val renderUsecase = mockRenderUsecase
  }

  describe("#execRender") {
    describe("指定のエンドポイントを叩くと、") {
      it("OK が返ってくること") {
        val expectHTML = Buf.Utf8("<h1>ダミーだにゃん</h1>")
        (mockRenderUsecase
          .exec(_: ConnpassEvent))
          .expects(*)
          .returning(IO(expectHTML))

        val maybeResult = mockEndpoint
          .create(new URL("https://cnd.connpass.com/event/dummy/"))(Input.get("/render"))
          .awaitOutputUnsafe()

        val actualStatus = maybeResult.map(_.status).get
        val actualHeader = maybeResult.map(_.headers).get

        actualStatus shouldBe Status.SeeOther
        actualHeader("Location") shouldBe "/index.html"
      }
    }
  }
}
