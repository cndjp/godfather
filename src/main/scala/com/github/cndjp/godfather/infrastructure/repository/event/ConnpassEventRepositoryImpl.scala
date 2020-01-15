package com.github.cndjp.godfather.infrastructure.repository.event

import java.io.IOException
import java.net.URL
import java.util.UUID

import cats.syntax.all._
import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import com.github.cndjp.godfather.domain.participant.ParticipantStatus.{
  CANCELLED,
  ORGANIZER,
  PARTICIPANT,
  WAITLISTED
}
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.domain.user_elements.UserElements
import com.github.cndjp.godfather.exception.GodfatherException.{
  GodfatherGeneralException,
  GodfatherRendererException
}
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.nodes.Element

class ConnpassEventRepositoryImpl extends ConnpassEventRepository with LazyLogging {
  // イベントのタイトルを持ってくる
  override def getEventTitle(event: ConnpassEvent): IO[String] =
    for {
      result <- try IO(
                 Jsoup
                   .connect(event.url.toString)
                   .get()
                   .select("meta[itemprop=name]")
                   .attr("content"))
               catch {
                 case e: IOException => IO.raiseError(GodfatherRendererException(e.getMessage))
               }
    } yield result

  // コンパスのイベントURLから登録者を持ってくる
  override def getElements(event: ConnpassEvent): IO[Seq[(ParticipantStatus, Elements)]] =
    for {
      document <- try IO(Jsoup.connect(event.getParticipantsListUrl).get())
                 catch {
                   case e: IOException => IO.raiseError(GodfatherRendererException(e.getMessage))
                 }
      result <- ParticipantStatus.values
                 .filterNot(_ == CANCELLED)
                 .foldLeft(IO.pure { Seq.empty[(ParticipantStatus, Elements)] }) { (init, status) =>
                   for {
                     initSeq <- init
                     doc <- status match {
                             case ORGANIZER =>
                               IO.pure {
                                 (ORGANIZER, document.select("div[class=concerned_area mb_30]"))
                               }
                             case PARTICIPANT =>
                               IO.pure {
                                 (
                                   PARTICIPANT,
                                   document.select("div[class=participation_table_area mb_20]"))
                               }
                             case WAITLISTED =>
                               IO.pure {
                                 (
                                   WAITLISTED,
                                   document.select("div[class=waitlist_table_area mb_20]"))
                               }
//                             case CANCELLED =>
//                               IO.pure {
//                                 elements :+= (CANCELLED, document.select(
//                                   "div[class=cancelled_table_area mb_20]"))
//                               }
                             case _ =>
                               IO.raiseError(
                                 GodfatherGeneralException("想定外のParticipantStatusを検知しました"))
                           }
                     appendedSeq <- IO(initSeq :+ doc)
                   } yield appendedSeq
                 }
    } yield result

}
