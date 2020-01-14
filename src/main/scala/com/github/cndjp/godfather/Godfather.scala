package com.github.cndjp.godfather

import java.net.URL
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import com.github.cndjp.godfather.endpoint.hc.HealthCheckEndpoint
import com.github.cndjp.godfather.endpoint.render.RenderEndpoint
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch.Application
import io.finch._

import scala.concurrent.ExecutionContext
import com.twitter.server.TwitterServer

object Godfather extends TwitterServer {

  val eventURL =
    flag("event-url", "", "Event URL (e.g. https://cnd.connpass.com/event/154414/)")

  def main(): Unit = {
    logger.info(s"レンダリングするイベントURL: ${eventURL()}")

    implicit val S: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    val api = Bootstrap
      .serve[Text.Plain](HealthCheckEndpoint.hc)
      .serve[Text.Html](RenderEndpoint.create(new URL(s"${eventURL()}")))
      .serve[Application.Javascript](Endpoint[IO].classpathAsset("/include.js"))
      .serve[Text.Html](Endpoint[IO].classpathAsset("/cards.html"))

    val server =
      Http.server.withAdmissionControl
        .concurrencyLimit(maxConcurrentRequests = 10, maxWaiters = 10)
        .serve(":8080", api.toService)

    onExit {
      logger.info("graceful shutdown...")
      server.close()
    }

    Await.ready(adminHttpServer)
  }
}
