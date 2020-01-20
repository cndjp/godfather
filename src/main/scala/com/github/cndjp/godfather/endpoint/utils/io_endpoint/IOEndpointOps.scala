package com.github.cndjp.godfather.endpoint.utils.io_endpoint

import cats.effect.IO
import com.github.cndjp.godfather.endpoint.utils.finchx.FinchxEndpointOps

trait IOEndpointOps extends FinchxEndpointOps[IO] {}
