package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import cats.effect.{IO, Resource}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository
import cats.effect.implicits._
import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository)
    extends RenderUsecase
    with LazyLogging {
  private[this] val resourcesPath = "./src/main/resources"

  override def exec(event: ConnpassEvent): IO[String] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- if (Files.exists(cardHTMLPath)) IO.unit else render(event, cardHTMLPath)
      indexHTML <- Resource
                    .fromAutoCloseable(IO(Source.fromFile(s"$resourcesPath/index.html")))
                    .use { file =>
                      IO(file.mkString)
                    }
    } yield indexHTML

  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      cardHTML <- IO { Files.createFile(cardHTMLPath) }
      participants <- connpassEventRepository.getParticipants(event)
      title <- connpassEventRepository.getEventTitle(event)
      output <- connpassEventRepository.participantList2String(title, participants)
      _ <- Resource
            .fromAutoCloseable(IO(new PrintWriter(cardHTML.toFile.getPath)))
            .use { pw =>
              IO(pw.write(output))
            }
      _ <- IO(logger.info("Finish for rendering!!"))
    } yield ()
}
