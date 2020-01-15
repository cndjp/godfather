package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import cats.effect.{IO, Resource, Timer}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import cats.syntax.all._
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherGeneralException
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
      // resourcesPath/cards.htmlを探しに行く
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))

      // resourcesPath/cards.htmlがあったらそのまま、なかったら作る
      cardHTML <- if (cardHTMLPath.toFile.exists) IO(cardHTMLPath.toFile)
                 else IO(Files.createFile(cardHTMLPath).toFile)

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
            .fromAutoCloseable(IO(new PrintWriter(cardHTML.getPath)))
            .use(pw => IO(pw.write(output)))
      _ <- IO(logger.info("Finish for rendering!!"))
    } yield ()
}
