package com.github.cndjp.godfather.usecase

import java.nio.file.Path

import better.files.File
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent

trait RenderUsecase {
  def exec(event: ConnpassEvent): IO[String]
}
