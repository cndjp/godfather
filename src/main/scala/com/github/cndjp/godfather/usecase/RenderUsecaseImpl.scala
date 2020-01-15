package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import cats.effect.{IO, Resource, Timer}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import cats.syntax.all._
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository,
                        connpassParticipantRepository: ConnpassParticipantRepository)
    extends RenderUsecase
    with LazyLogging {
  private[this] val resourcesPath = "./src/main/resources"

  // cards.htmlがレンダリングして最後にindex.htmlを返す
  override def exec(event: ConnpassEvent): IO[Unit] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- render(event, cardHTMLPath)
    } yield ()

  // レンダリングをやる
  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      // 登録者をconnpassのページからfetchしてくる
      elements <- connpassEventRepository.getElements(event)

      // 登録者のHTMLエレメントをparticipantクラスに変換する
      participants <- connpassParticipantRepository.element2Participants(elements)

      // イベントのタイトルをconnpassのページからfetchしてくる
      title <- connpassEventRepository.getEventTitle(event)

      // 登録者とイベントのタイトルをパースしてcards.htmlのファイルに書き込むHTMLの文字列を持ってくる
      output <- connpassParticipantRepository.parseParticipantList(title, participants)

      // 最後にoutputをcards.htmlのファイルに書き込む
      _ <- Resource
            .fromAutoCloseable(IO(new PrintWriter(cardHTMLPath.toFile.getPath)))
            .use(pw => IO(pw.write(output)))
      _ <- IO(logger.info("Finish for rendering!!"))
    } yield ()
}
