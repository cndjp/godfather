package com.github.cndjp.godfather.endpoint.render

import java.net.URL

import better.files.File
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.endpoint.interface.GodfatherInterface
import com.github.cndjp.godfather.endpoint.utils.io_endpoint.IOEndpointOps
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherGeneralException
import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import com.typesafe.scalalogging.LazyLogging
import io.finch.{Endpoint, _}

object RenderEndpoint extends IOEndpointOps with LazyLogging with GodfatherInterface {
  def create(url: URL) = execRender(url)

  private def execRender(url: URL): Endpoint[IO, Buf] =
    get("render") {
      renderUsecase
        .exec(ConnpassEvent(url))
        .attempt
        .map {
          case Left(err) =>
            logger.error("render", err)
            UnprocessableEntity(GodfatherGeneralException(err.getMessage))
          case Right(html) => Ok(Buf.Utf8(html))
        }
    }
}
