package com.github.cndjp.godfather.infrastructure.repository.event

import cats.effect.IO
import com.github.cndjp.godfather.domain.elements.participants.ParticipantsElements
import com.github.cndjp.godfather.domain.event.{ConnpassEvent, ConnpassTitle}
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import org.jsoup.select.Elements
import com.github.cndjp.godfather.domain.participant.ParticipantStatus.{
  CANCELLED,
  ORGANIZER,
  PARTICIPANT,
  WAITLISTED
}
import com.github.cndjp.godfather.domain.repository.event.ConnpassEventRepository
import com.github.cndjp.godfather.exception.GodfatherException.{
  GodfatherGeneralException,
  GodfatherRendererException
}
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.nodes.Element

class ConnpassEventRepositoryImpl(scrapeAdapter: ScrapeAdapter)
    extends ConnpassEventRepository
    with LazyLogging {
  // イベントのタイトルを持ってくる
  override def getEventTitle(event: ConnpassEvent): IO[ConnpassTitle] =
    for {
      result <- scrapeAdapter.getDocument(event.url.toString).flatMap {
                 case Right(doc) => IO(doc.select("meta[itemprop=name]").attr("content"))
                 case Left(e)    => IO.raiseError(GodfatherRendererException(e.getMessage))
               }
    } yield ConnpassTitle(result)

  // コンパスのイベントURLから登録者を持ってくる
  override def getParticipantElements(
      event: ConnpassEvent): IO[Map[ParticipantStatus, ParticipantsElements]] =
    for {
      document <- scrapeAdapter.getDocument(event.getParticipantsListUrl).flatMap {
                   case Right(doc) => IO.pure(doc)
                   case Left(e)    => IO.raiseError(GodfatherRendererException(e.getMessage))
                 }
      result <- ParticipantStatus.values
                 .filterNot(_ == CANCELLED)
                 .foldLeft(IO.pure { Map.empty[ParticipantStatus, ParticipantsElements] }) {
                   (init, status) =>
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
                       pDoc <- createParticipantsElements(doc._2)
                       appendedSeq <- IO(initSeq + (doc._1 -> pDoc))
                     } yield appendedSeq
                 }
    } yield result

  // connpassのURLからfetchしてきたHTMLエレメントを加工して、利用しやすい形の登録者全員のHTMLにして返す
  private[this] def createParticipantsElements(elems: Elements): IO[ParticipantsElements] =
    for {
      result <- elems
                 .toArray(Array.empty[Element])
                 .foldLeft(IO.pure(new Elements())) { (init, item) =>
                   for {
                     initElems <- init
                     paginatedUserListLink <- IO(item.select("tr.empty td[colspan=2] a"))
                     _ <- paginatedUserListLink.isEmpty match {
                           // paginatedUserListLinkがContain
                           case true => IO(initElems.add(item))
                           // paginatedUserListLinkがEmpty
                           case false =>
                             val paginatedUserListUrl =
                               paginatedUserListLink
                                 .first()
                                 .attr("href")
                             (paginatedUserListUrl == null || !paginatedUserListUrl.contains(
                               "/ptype/")) match {
                               // paginatedUserListUrlがnullじゃないし "/ptype/" を含んでもない文字列
                               case true => IO(initElems.add(item))
                               // paginatedUserListUrlがnull、か "/ptype/" を含む文字列
                               case false =>
                                 for {
                                   maybePage1 <- scrapeAdapter
                                                  .getDocument(paginatedUserListUrl)
                                   _ <- maybePage1.fold(
                                         error => IO(logger.error(error.getMessage)),
                                         page1 =>
                                           for {
                                             _ <- IO(initElems.add(page1))
                                             participantsCount = page1
                                               .select("span.participants_count")
                                               .text()
                                               .replace("人", "")
                                               .toInt
                                             lastPage = participantsCount / 100 + 1
                                             _ <- (2 to lastPage)
                                                   .foldLeft(IO.unit) { (init, page) =>
                                                     for {
                                                       _ <- init
                                                       _ <- scrapeAdapter
                                                             .getDocument(
                                                               paginatedUserListUrl + "?page=" + page)
                                                             .flatMap {
                                                               case Right(doc) =>
                                                                 IO(initElems.add(doc))
                                                               case Left(e) =>
                                                                 IO(logger.error(e.getMessage))
                                                             }
                                                     } yield ()
                                                   }
                                           } yield ()
                                       )
                                 } yield ()
                             }
                         }
                   } yield initElems
                 }
      users <- IO.pure(result.select("td.user .user_info"))
    } yield ParticipantsElements(users)
}
