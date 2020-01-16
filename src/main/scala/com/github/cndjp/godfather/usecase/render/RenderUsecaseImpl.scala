package com.github.cndjp.godfather.usecase.render

import better.files._
import cats.effect.{IO, Resource}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.usecase.utils.GodfatherUsecaseUtils
import com.typesafe.scalalogging.LazyLogging
import cats.implicits._

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository,
                        connpassParticipantRepository: ConnpassParticipantRepository)
    extends RenderUsecase
    with GodfatherUsecaseUtils
    with LazyLogging {
  // cards.htmlがレンダリングして最後にindex.htmlを返す

  override def exec(event: ConnpassEvent): IO[Unit] =
    for {
      // resourcesPath/cards.htmlがあったらそのまま、なかったら作る
      cardHTML <- IO(File(s"$resourcesPath/cards.html").createFileIfNotExists())

      // 登録者をconnpassのページからfetchしてくる
      elements <- connpassEventRepository.getElements(event)

      // 登録者のHTMLエレメントをparticipantクラスに変換する
      participants <- connpassParticipantRepository.element2Participants(elements)

      // イベントのタイトルをconnpassのページからfetchしてくる
      title <- connpassEventRepository.getEventTitle(event)

      // 登録者とイベントのタイトルをパースしてcards.htmlのファイルに書き込むHTMLの文字列を持ってくる
      output <- connpassParticipantRepository.parseParticipantList(title, participants)

      // 最後にoutputをcards.htmlのファイルに書き込む
      _ <- IO(cardHTML.write(output)) *> IO(logger.info("Finish for rendering!!⭐️"))
    } yield ()

}
