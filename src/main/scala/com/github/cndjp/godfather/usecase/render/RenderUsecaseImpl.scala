package com.github.cndjp.godfather.usecase.render

import better.files._
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.typesafe.scalalogging.LazyLogging
import cats.implicits._
import com.github.cndjp.godfather.domain.participant.ConnpassParticipant

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository,
                        connpassParticipantRepository: ConnpassParticipantRepository)
    extends RenderUsecase
    with LazyLogging {

  // cards.htmlがレンダリングして最後にindex.htmlを返す
  override def exec(event: ConnpassEvent)(implicit resourcesPath: String): IO[Unit] = {
    val maybeCardHTML = File(s"$resourcesPath/cards.html")
    for {
      // resourcesPath/cards.htmlがあったら何もしない
      _ <- if (maybeCardHTML.exists) IO(logger.info("cards.html is already exist!!")) *> IO.unit
          else
            for {
              // resourcesPath/cards.htmlがなかったから作る
              cardHTML <- IO.suspend(IO(maybeCardHTML.createFile()))

              // 登録者をconnpassのページからfetchしてくる
              elements <- connpassEventRepository.getParticipantElements(event)

              // 登録者のHTMLエレメントをparticipantクラスに変換する
              participants <- elements.foldLeft(IO.pure(Seq.empty[ConnpassParticipant])) {
                               (init, item) =>
                                 for {
                                   initSeq <- init
                                   elem <- IO(logger.info(
                                            s"Collect Participants: [${item._1.name}]")) *> connpassParticipantRepository
                                            .element2Participant(item._2)
                                   appendedSeq <- IO(initSeq ++ elem)
                                 } yield appendedSeq
                             }

              // イベントのタイトルをconnpassのページからfetchしてくる
              title <- connpassEventRepository.getEventTitle(event)

              // 登録者とイベントのタイトルをパースしてcards.htmlのファイルに書き込むHTMLの文字列を持ってくる
              cards <- connpassParticipantRepository.renderParticipantList(title, participants)

              // 最後にoutputをcards.htmlのファイルに書き込む
              _ <- IO(cardHTML.write(cards.doc)) *> IO(logger.info("Finish for rendering!!⭐️"))
            } yield ()
    } yield ()
  }
}
