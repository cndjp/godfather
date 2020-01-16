package com.github.cndjp.godfather

import java.net.URL
import com.github.cndjp.godfather.endpoint.hc.HealthCheckEndpoint
import com.github.cndjp.godfather.endpoint.render.RenderEndpoint
import com.github.cndjp.godfather.endpoint.resource.ResourceEndpoint
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch.Application
import io.finch._

import com.twitter.server.TwitterServer

object Godfather extends TwitterServer {

  val eventURL =
    flag("event-url", "", "Event URL (e.g. https://cnd.connpass.com/event/154414/)")

  def main(): Unit = {
    logger.info(s"レンダリングするイベントURL: ${eventURL()}")
    val renderEndpoint = new RenderEndpoint
    val healthCheckEndpoint = new HealthCheckEndpoint
    val resourceEndpoint = new ResourceEndpoint

    val api = Bootstrap
      .serve[Text.Plain](healthCheckEndpoint.hc)
      .serve[Text.Plain](renderEndpoint.create(new URL(s"${eventURL()}")))
      .serve[Application.Javascript](resourceEndpoint.createContentJS)
      .serve[Text.Html](resourceEndpoint.createContentHTML)

    val server =
      Http.server.withAdmissionControl
        .concurrencyLimit(maxConcurrentRequests = 10, maxWaiters = 10)
        .serve(":8080", api.toService)

    onExit {
      logger.info("graceful shutdown...")
      server.close()
    }

    logger.info(s"Godfather Ready!! ☕️")
    Await.ready(adminHttpServer)
  }
}
