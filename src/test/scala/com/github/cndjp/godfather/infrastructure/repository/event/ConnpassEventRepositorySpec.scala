package com.github.cndjp.godfather.infrastructure.repository.event

import com.github.cndjp.godfather.domain.adapter.scrape.ScrapeAdapter
import com.github.cndjp.godfather.support.GodfatherTestSupport

class ConnpassEventRepositorySpec extends GodfatherTestSupport {
  val mockScrapeAdapter = mock[ScrapeAdapter]
  val mockConnpassEventRepository = new ConnpassEventRepositoryImpl(mockScrapeAdapter)

  describe("#getEventTitle") {
    describe("指定のエンドポイントを叩くと、") {
      it("OK が返ってくること") {}
    }
  }
}
