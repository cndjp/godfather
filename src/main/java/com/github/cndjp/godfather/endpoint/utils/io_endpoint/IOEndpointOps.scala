package com.github.cndjp.godfather.endpoint.utils.io_endpoint

import cats.effect.IO
import com.github.cndjp.godfather.endpoint.utils.finchx.FinchxEndpointOps
import io.finch.Endpoint
import shapeless.HNil

trait IOEndpointOps extends FinchxEndpointOps[IO] {
  // add `/api/v1`
  protected val rootPath: Endpoint[IO, HNil] = path("api") :: path("v1")
}
