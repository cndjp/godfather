package com.github.cndjp.godfather

import java.net.URL
import com.github.cndjp.godfather.config.GodfatherCliConfig
import com.github.cndjp.godfather.event.Event
import com.github.cndjp.godfather.exception.GodfatherException.{GeneralGodfatherException, GodfatherEventException, GodfatherRendererException}
import com.github.cndjp.godfather.preview.PreviewServer
import com.github.cndjp.godfather.preview.renderer.Cards
import com.typesafe.scalalogging.LazyLogging
import scopt.OParser

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
      val server = PreviewServer.getInstance();
      try {
        new Cards().event(Event.getEvent(config.eventURL)).render();
        Runtime.getRuntime.addShutdownHook(new Thread{
          () -> {
            server.stop();
            Cards.flashCards();
          }
        })
        server.start(8080);
      } catch {
        case GeneralGodfatherException(err) => logger.error("一般エラー", err);
        case GodfatherEventException(err) => logger.error("イベントエラー", err);
        case GodfatherRendererException(err) => logger.error("レンダリングエラー", err);
        case _ => logger.error("予期せぬエラー")
      }
    }
    case _ =>
      logger.error("コマンドラインのパースに失敗しました")
  }
}
