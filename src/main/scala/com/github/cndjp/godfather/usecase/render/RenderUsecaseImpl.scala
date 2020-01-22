package com.github.cndjp.godfather.usecase.render

import better.files._
import cats.effect.{IO, Resource}
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.typesafe.scalalogging.LazyLogging
import cats.implicits._
import com.github.cndjp.godfather.domain.participant.ConnpassParticipant
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository,
                        connpassParticipantRepository: ConnpassParticipantRepository)
    extends RenderUsecase
    with LazyLogging {
  // cards.htmlがレンダリングして最後にindex.htmlを返す

  override def exec(event: ConnpassEvent)(implicit resourcesPath: String): IO[Unit] =
    for {
      // resourcesPath/cards.htmlがあったらそのまま、なかったら作る
      cardHTML <- IO(File(s"$resourcesPath/cards.html").createFileIfNotExists())

      // 登録者をconnpassのページからfetchしてくる
      elements <- connpassEventRepository.getParticipantElements(event)

      // 登録者のHTMLエレメントをparticipantクラスに変換する
      participants <- elements.foldLeft(IO.pure(Seq.empty[ConnpassParticipant])) { (init, item) =>
                       for {
                         initSeq <- init
                         elem <- IO(logger.info(s"Collect Participants: [${item._1.name}]")) *> connpassParticipantRepository
                                  .element2Participant(item._2)
                         appendedSeq <- IO(initSeq ++ elem)
                       } yield appendedSeq
                     }

      // イベントのタイトルをconnpassのページからfetchしてくる
      title <- connpassEventRepository.getEventTitle(event)

      // 登録者とイベントのタイトルをパースしてcards.htmlのファイルに書き込むHTMLの文字列を持ってくる
      cards <- connpassParticipantRepository.renderParticipantList(title.value, participants)

      // 最後にoutputをcards.htmlのファイルに書き込む
      _ <- IO(cardHTML.write(cards.doc)) *> IO(logger.info("Finish for rendering!!⭐️"))
    } yield ()

}
