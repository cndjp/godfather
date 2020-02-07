package com.github.cndjp.godfather.utils

object BooleanUtils {
  implicit class BooleanToEither(val src: Boolean) {

    def toEither: Either[Unit, Unit] =
      if (src) Right(Unit)
      else Left(Unit)
  }
}
