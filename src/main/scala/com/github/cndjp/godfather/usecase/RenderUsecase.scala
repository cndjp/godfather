package com.github.cndjp.godfather.usecase

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.twitter.io.Buf

trait RenderUsecase {
  def exec(event: ConnpassEvent): IO[Buf]
}
