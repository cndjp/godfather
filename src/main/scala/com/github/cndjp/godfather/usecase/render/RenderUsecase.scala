package com.github.cndjp.godfather.usecase.render

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.twitter.io.Buf

trait RenderUsecase {
  lazy val resourcesPath = "./src/main/resources"
  def exec(event: ConnpassEvent): IO[Buf]
}
