package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import cats.implicits._
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository
import scala.io.Source

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository) extends RenderUsecase {
  private[this] val resourcesPath = "./src/main/resources"

  override def exec(event: ConnpassEvent): IO[String] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- if (Files.exists(cardHTMLPath)) IO.unit else render(event, cardHTMLPath)
      indexHTML <- IO(Source.fromFile(s"$resourcesPath/index.html"))
                    .bracket(file => IO(file.mkString))(file => IO(file.close()))
    } yield indexHTML

  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      cardHTML <- IO { Files.createFile(cardHTMLPath) }
      participants <- connpassEventRepository.getParticipants(event)
      title <- connpassEventRepository.getEventTitle(event)
      output <- connpassEventRepository.participantList2String(title, participants)
      _ <- IO(new PrintWriter(cardHTML.toFile.getPath))
            .bracket(pw => IO(pw.write(output)))(pw => IO(pw.close())) *> IO.unit
    } yield ()
}
