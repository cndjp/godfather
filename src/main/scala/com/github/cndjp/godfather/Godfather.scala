package com.github.cndjp.godfather

import java.net.URL

import cats.effect.{ExitCode, IO, IOApp}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.endpoint.hc.HealthCheckEndpoint
import com.github.cndjp.godfather.endpoint.resource.ResourceEndpoint
import com.github.cndjp.godfather.iface.GodfatherInterface
import com.twitter.finagle.Http
import io.finch.Application
import io.finch._
import cats.implicits._
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherParseArgsException
import com.typesafe.scalalogging.LazyLogging
import com.twitter.util.Await
import scopt.OptionParser

object Godfather extends GodfatherInterface with IOApp with LazyLogging {

  val healthCheckEndpoint = new HealthCheckEndpoint
  val resourceEndpoint = new ResourceEndpoint

  val api = Bootstrap
    .serve[Text.Plain](healthCheckEndpoint.hc)
    .serve[Application.Javascript](resourceEndpoint.createContentJS)
    .serve[Text.Html](resourceEndpoint.createContentHTML)

  val server =
    Http.server.withAdmissionControl
      .concurrencyLimit(maxConcurrentRequests = 10, maxWaiters = 10)
      .serve(":8080", api.toService)

  override def run(args: List[String]): IO[ExitCode] = {
    import com.github.cndjp.godfather.utils.ResourcesImplicits.mainResourcesPath._

    case class Ops(url: String = "")

    def initCmdParse: OptionParser[Ops] =
      new OptionParser[Ops]("godfather") {
        opt[String]('e', "event-url")
          .action((x, c) => c.copy(url = x))
          .text("""
                  |  <value>: scrape connpass URL
                  |           (e.g. https://cnd.connpass.com/event/154414/)
                  |""".stripMargin)
      }

    for {
      eventURL <- initCmdParse.parse(args, Ops()).map(_.url) match {
                   case Some(v) => IO(logger.info(s"Scrape URL: $v")) *> IO.pure(v)
                   case None    => IO.raiseError(GodfatherParseArgsException(args.mkString(",")))
                 }

      _ <- renderUsecase
            .exec(ConnpassEvent(new URL(eventURL)))

      _ <- IO {
            logger.info(s"Godfather Ready!! ☕️")
            logger.info(s"Please Check it 👉 http://localhost:8080/index.html")
          }

      result <- IO(Await.ready(server)) *> IO.pure(ExitCode.Success)
    } yield result
  }
}
