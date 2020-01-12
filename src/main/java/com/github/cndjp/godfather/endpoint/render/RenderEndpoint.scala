package com.github.cndjp.godfather.endpoint.render

import cats.effect.IO
import com.github.cndjp.godfather.endpoint.interface.GodfatherInterface
import com.github.cndjp.godfather.endpoint.utils.io_endpoint.IOEndpointOps
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherGeneralException
import com.twitter.finagle.http.Response
import com.typesafe.scalalogging.LazyLogging
import io.finch.{Endpoint, _}

object RenderEndpoint extends IOEndpointOps with LazyLogging with GodfatherInterface{
  def create = execRender

  private def execRender: Endpoint[IO, Response] =
    get(rootPath :: "rendered") {
      renderUsecase
        .render
        .attempt
        .map{
          case Left(err) => UnprocessableEntity(GodfatherGeneralException(err.getMessage))
          case Right(_)  => Ok(Response())
        }
    }
}
