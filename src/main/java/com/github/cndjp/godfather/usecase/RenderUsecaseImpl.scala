package com.github.cndjp.godfather.usecase
import better.files.File
import cats.effect.IO

class RenderUsecaseImpl extends RenderUsecase {
  override def render: IO[Unit] =
    for {
      tmpFile <- IO(File.newTemporaryFile())

    } yield ()
}
