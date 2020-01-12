package com.github.cndjp.godfather.usecase

import better.files.File
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent

trait RenderUsecase {
  def render(event: ConnpassEvent): IO[String]
}
