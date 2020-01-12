package com.github.cndjp.godfather.usecase

import cats.effect.IO

trait RenderUsecase {
  def render: IO[Unit]
}
