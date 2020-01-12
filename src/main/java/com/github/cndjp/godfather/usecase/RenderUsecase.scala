package com.github.cndjp.godfather.usecase

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent

trait RenderUsecase {
  def render(event: ConnpassEvent): IO[Unit]
}
