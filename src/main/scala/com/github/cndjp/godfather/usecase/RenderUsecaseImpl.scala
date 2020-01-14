package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.{Executor, Executors}

import cats.effect.{IO, Resource, Timer}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository
import cats.effect.implicits._
import cats.syntax.all._
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherGeneralException
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.io.Source

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository)
    extends RenderUsecase
    with LazyLogging {
  private[this] val resourcesPath = "./src/main/resources"

  // cards.htmlがあったらレンダリング、なかったら何にもしない
  override def execOrIgnore(event: ConnpassEvent): IO[Unit] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- if (Files.exists(cardHTMLPath)) IO.unit else render(event, cardHTMLPath)
    } yield ()

  // レンダリングをやる
  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      // cards.htmlのファイルを作る
      cardHTML <- IO(Files.createFile(cardHTMLPath))

      // 登録者をconnpassのページからfetchしてくる
      participants <- connpassEventRepository.getParticipants(event)

      // イベントのタイトルをconnpassのページからfetchしてくる
      title <- connpassEventRepository.getEventTitle(event)

      // 登録者とイベントのタイトルをパースしてcards.htmlのファイルに書き込むHTMLの文字列を持ってくる
      output <- connpassEventRepository.parseParticipantList(title, participants)

      // 最後にoutputをcards.htmlのファイルに書き込む
      _ <- Resource
            .fromAutoCloseable(IO(new PrintWriter(cardHTML.toFile.getPath)))
            .use { pw =>
              IO(pw.write(output)) *> IO.unit
            }
      _ <- IO(logger.info("Finish for rendering!!"))
    } yield ()
}
