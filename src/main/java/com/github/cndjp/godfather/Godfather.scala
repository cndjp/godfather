package com.github.cndjp.godfather

import java.net.URL

import cats.effect.{ContextShift, IO}
import com.github.cndjp.godfather.config.GodfatherCliConfig
import com.github.cndjp.godfather.endpoint.hc.HealthCheckEndpoint
import com.github.cndjp.godfather.endpoint.render.RenderEndpoint
import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Await
import com.typesafe.scalalogging.LazyLogging
import io.finch.Application
import scopt.OParser
import io.finch._
import scala.concurrent.ExecutionContext

object Godfather extends App with LazyLogging {
  val builder = OParser.builder[GodfatherCliConfig]

  val parser = {
    import builder._
    OParser.sequence(
      programName("godfather"),
      head("godfather", "0.1"),
      opt[URL]('e', "event-url")
        .required()
        .action((x, c) => c.copy(eventURL = x))
        .valueName("<url>")
        .text("Event URL (e.g. https://cnd.connpass.com/event/154414/)")
    )
  }
  OParser.parse(parser, args, GodfatherCliConfig()) match {
    case Some(config) => {
      logger.info(s"レンダリングするイベントURL: ${config.eventURL}")
//      val server = PreviewServer.getInstance();
//      try {
//        new Cards().event(Event.getEvent(config.eventURL)).render();
//        Runtime.getRuntime.addShutdownHook(new Thread {
//          () -> {
//            server.stop();
//            Cards.flashCards();
//          }
//        })
//        server.start(8080);
      val policy = Cors.Policy(
        allowsOrigin = _ => Some("*"),
        allowsMethods = _ => Some(Seq("GET", "POST", "PUT", "PATCH", "OPTIONS")),
        allowsHeaders = _ => Some(Seq("Accept", "Content-Type"))
      )

      implicit val S: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      val api = Bootstrap
        .serve[Text.Plain](HealthCheckEndpoint.hc)
        .serve[Text.Html](RenderEndpoint.create(config.eventURL))
        .serve[Application.Javascript](Endpoint[IO].classpathAsset("/include.js"))
        .serve[Text.Html](Endpoint[IO].classpathAsset("/index.html"))
        .serve[Text.Html](Endpoint[IO].classpathAsset("/cards.html"))

      Await.ready(
        Http.serve(
          ":8080",
          new Cors.HttpFilter(policy)
            .andThen(api.toService)
        )
      )
//      } catch {
//        case GodfatherGeneralException(err)  => logger.error("一般エラー", err)
//        case GodfatherEventException(err)    => logger.error("イベントエラー", err)
//        case GodfatherRendererException(err) => logger.error("レンダリングエラー", err)
//        case e: Throwable                    => logger.error(s"予期せぬエラー: ${e.getStackTrace}")
//        case _                               => logger.error("予期せぬエラー")
//      }
    }
    case _ =>
      logger.error("コマンドラインのパースに失敗しました")
  }
}
