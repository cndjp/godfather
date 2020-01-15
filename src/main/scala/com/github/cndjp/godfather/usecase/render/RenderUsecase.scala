package com.github.cndjp.godfather.usecase.render

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.resourcePath.ResourcePath
import com.twitter.io.Buf

trait RenderUsecase {
  def exec(event: ConnpassEvent): IO[Buf]
}
