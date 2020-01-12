package com.github.cndjp.godfather.infrastructure.repository.connpass_event

import java.io.IOException
import java.net.URL
import java.util.UUID

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import cats.syntax.all._
import com.github.cndjp.godfather.domain.participant.ParticipantStatus.{
  CANCELLED,
  ORGANIZER,
  PARTICIPANT,
  WAITLISTED
}
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherGeneralException
import org.jsoup.nodes.Element

object ConnpassEventRepositoryImpl {
  private[this] val IMAGE_SOURCE_DEFAULT = new URL(
    "https://connpass.com/static/img/common/user_no_image_180.png")

  def getParticipants(event: ConnpassEvent): IO[Seq[ConnpassParticipant]] = {
    var elements = Seq[(ParticipantStatus, Elements)]()
    for {
      document <- try {
                   IO(Jsoup.connect(event.getParticipantsListUrl).get())
                 } catch {
                   case e: IOException => IO.raiseError(e)
                 }
      _ <- IO.pure(ParticipantStatus.values.foreach {
            case ORGANIZER =>
              elements :+= (ORGANIZER, document.select("div[class=concerned_area mb_30]"))
            case PARTICIPANT =>
              elements :+= (PARTICIPANT, document.select(
                "div[class=participation_table_area mb_20]"))
            case WAITLISTED =>
              elements :+= (WAITLISTED, document.select("div[class=waitlist_table_area mb_20]"))
            case CANCELLED =>
              elements :+= (CANCELLED, document.select("div[class=cancelled_table_area mb_20]"))
            case _ => IO.raiseError(GodfatherGeneralException("想定外のParticipantStatusを検知しました"))
          })
      result <- element2Participants(elements)
    } yield result
  }

  private[this] def element2Participants(
      input: Seq[(ParticipantStatus, Elements)]): IO[Seq[ConnpassParticipant]] =
    for {
      result <- input.foldLeft(IO(Seq[ConnpassParticipant]())) {
                 val userTableElementsConsideringPagination = new Elements()
                 (init, items) =>
                   for {
                     initSeq <- init
                     _ <- IO {
                           items._2.toArray(Array[Element]()).foreach { item =>
                             val paginatedUserListLink = item.select("tr.empty td[colspan=2] a")
                             if (paginatedUserListLink.isEmpty)
                               userTableElementsConsideringPagination.add(item)
                             else {
                               val paginatedUserListUrl = paginatedUserListLink.first().attr("href")
                               if (paginatedUserListUrl == null || !paginatedUserListUrl.contains(
                                     "/ptype/")) userTableElementsConsideringPagination.add(item)
                               else {
                                 val page1 = Jsoup.connect(paginatedUserListUrl).get()
                                 userTableElementsConsideringPagination.add(page1)
                                 val participantsCount = page1
                                   .select("span.participants_count")
                                   .text()
                                   .replace("人", "")
                                   .toInt
                                 val lastPage = participantsCount / 100 + 1
                                 var i = 2
                                 while (i <= lastPage) {
                                   val pageX =
                                     Jsoup.connect(paginatedUserListUrl + "?page=" + i).get()
                                   userTableElementsConsideringPagination.add(pageX)
                                   i += 1
                                 }
                               }
                             }
                           }
                         }
                     users <- IO.pure(
                               userTableElementsConsideringPagination.select("td.user .user_info"))
                     participant <- IO {
                                     users.toArray(Array[Element]()).map {
                                       var userCounter = 0
                                       user =>
                                         {
                                           val displayName = user.select("p.display_name a").text()
                                           val userHome =
                                             Jsoup
                                               .connect(
                                                 user.select("p.display_name a").attr("href"))
                                               .get()
                                           val images = userHome.select(
                                             "div[id=side_area] div[class=mb_20 text_center] a.image_link")
                                           val imageSource =
                                             if (!images.isEmpty)
                                               images
                                                 .toArray(Array[Element]())
                                                 .find(_.attr("href").contains("/user/"))
                                                 .map(image => new URL(image.attr("href")))
                                                 .getOrElse(IMAGE_SOURCE_DEFAULT)
                                             else IMAGE_SOURCE_DEFAULT

                                           userCounter += 1
                                           System.out.println(
                                             displayName + "(" + items._1.getName + "): " + userCounter + "/" + users
                                               .size())
                                           ConnpassParticipant(
                                             UUID.randomUUID().toString,
                                             displayName,
                                             imageSource,
                                             items._1)
                                         }
                                     }
                                   }
                     appendedInitSeq <- IO(initSeq ++ participant.toSeq)
                   } yield appendedInitSeq
               }
    } yield result

  def getParticipantsWithoutCancelled: Seq[ConnpassParticipant] = ???
}
