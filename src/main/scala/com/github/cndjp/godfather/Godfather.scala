package com.github.cndjp.godfather

import java.net.URL

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.endpoint.hc.HealthCheckEndpoint
import com.github.cndjp.godfather.endpoint.resource.ResourceEndpoint
import com.github.cndjp.godfather.iface.GodfatherInterface
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch.Application
import io.finch._
import com.twitter.server.TwitterServer

object Godfather extends TwitterServer with GodfatherInterface {

  val eventURL =
    flag("event-url", "", "Event URL (e.g. https://cnd.connpass.com/event/154414/)")

  def main(): Unit = {
    import com.github.cndjp.godfather.utils.ResourcesImplicits.mainResourcesPath._

    logger.info(s"Scrape URL: ${eventURL()}")
    val healthCheckEndpoint = new HealthCheckEndpoint
    val resourceEndpoint = new ResourceEndpoint

    renderUsecase
      .exec(ConnpassEvent(new URL(s"${eventURL()}")))
      .handleErrorWith(e => IO(logger.warn(e.getMessage)))
      .unsafeRunSync()

    val api = Bootstrap
      .serve[Text.Plain](healthCheckEndpoint.hc)
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
    logger.info(s"Please Click it 👉 http://localhost:8080/index.html")
    Await.ready(adminHttpServer)
  }
}
