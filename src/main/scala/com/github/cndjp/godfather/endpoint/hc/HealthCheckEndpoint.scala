package com.github.cndjp.godfather.endpoint.hc

import cats.effect.IO
import com.github.cndjp.godfather.endpoint.utils.io_endpoint.IOEndpointOps
import com.typesafe.scalalogging.LazyLogging
import io.finch.{Endpoint, _}

object HealthCheckEndpoint extends IOEndpointOps with LazyLogging {
  def hc: Endpoint[IO, String] = get("hc") { Ok("OK") }
}
