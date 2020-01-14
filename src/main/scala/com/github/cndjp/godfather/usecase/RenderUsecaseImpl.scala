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

  // cards.htmlがあったらレンダリング、なかったら何にもしない、で最後にindex.htmlを返す
  override def execOrIgnore(event: ConnpassEvent): IO[String] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- if (Files.exists(cardHTMLPath)) IO.unit else render(event, cardHTMLPath)
      indexHTML <- Resource
                    .fromAutoCloseable(IO(Source.fromFile(s"$resourcesPath/index.html")))
                    .use(file => IO(file.mkString))
    } yield indexHTML

  // レンダリングをやる
  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      // cards.htmlのファイルを作る
      cardHTML <- IO(Files.createFile(cardHTMLPath))

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
            .fromAutoCloseable(IO(new PrintWriter(cardHTML.toFile.getPath)))
            .use(pw => IO(pw.write(output)))
      _ <- IO(logger.info("Finish for rendering!!"))
    } yield ()
}
