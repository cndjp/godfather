package com.github.cndjp.godfather.infrastructure.repository.connpass_event

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
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository
import com.github.cndjp.godfather.exception.GodfatherException.{
  GodfatherGeneralException,
  GodfatherRendererException
}
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.nodes.Element

class ConnpassEventRepositoryImpl extends ConnpassEventRepository with LazyLogging {
  private[this] val IMAGE_SOURCE_DEFAULT = new URL(
    "https://connpass.com/static/img/common/user_no_image_180.png")

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

  override def getParticipants(event: ConnpassEvent): IO[Seq[ConnpassParticipant]] = {
    var elements = Seq[(ParticipantStatus, Elements)]()
    for {
      document <- try IO(Jsoup.connect(event.getParticipantsListUrl).get())
                 catch {
                   case e: IOException => IO.raiseError(GodfatherRendererException(e.getMessage))
                 }
      _ <- IO.pure(ParticipantStatus.values.filterNot(_ == CANCELLED).foreach {
            case ORGANIZER =>
              elements :+= (ORGANIZER, document.select("div[class=concerned_area mb_30]"))
            case PARTICIPANT =>
              elements :+= (PARTICIPANT, document.select(
                "div[class=participation_table_area mb_20]"))
            case WAITLISTED =>
              elements :+= (WAITLISTED, document.select("div[class=waitlist_table_area mb_20]"))
//          case CANCELLED =>
//              elements :+= (CANCELLED, document.select("div[class=cancelled_table_area mb_20]"))
            case _ => IO.raiseError(GodfatherGeneralException("想定外のParticipantStatusを検知しました"))
          })
      result <- element2Participants(elements)
    } yield result
  }

  override def participantList2String(title: String, input: Seq[ConnpassParticipant]): IO[String] =
    for {
      adjust <- IO {
                 if (input.size % 2 == 1)
                   input :+ ConnpassParticipant("", "", null, ParticipantStatus.CANCELLED);
                 else input
               }
      factory <- IO {
                  (0 until (input.size / 2))
                    .foldLeft(Seq[(Int, ConnpassParticipant, ConnpassParticipant)]()) {
                      (init, counter) =>
                        val index = counter * 2
                        init :+ (index, adjust(index), adjust(index + 1))
                    }
                }
      result <- factory.foldLeft(IO.pure { Seq[String]("""<div class="container border">""") }) {
                 (r, item) =>
                   for {
                     rSeq <- r
                     renderUnit <- IO {
                                    var unit = Seq[String]()
                                    unit :+= """    <div class="row align-items-center border"> """
                                    unit :+= """        <div class="col-md-6 py-2 bg-info text-light"> """
                                    unit :+= """            <h4 class="text-center">""" + title + "</h4>"
                                    unit :+= """        </div> """
                                    unit :+= """        <div class="col-md-6 py-2 bg-info text-light border-left"> """
                                    unit :+= """            <h4 class="text-center">""" + title + "</h4>"
                                    unit :+= """        </div> """
                                    unit :+= """    </div> """
                                    unit :+= """    <div class="row align-items-center border"> """
                                    unit :+= """        <div class="col-md-2"> """
                                    unit :+= """            <img src=" """ + item._2.imageURL + "\""
                                    unit :+= """                 class="rounded" """
                                    unit :+= """                 width="160" height="160" """
                                    unit :+= """                 style="margin:20px 5px; object-fit:cover"/> """
                                    unit :+= """        </div> """
                                    unit :+= """        <div class="col-md-4 text-dark"> """
                                    unit :+= """            <h2 class="text-center">""" + item._2.name + "</h2>"
                                    unit :+= """        </div> """
                                    unit :+= """        <div class="col-md-2 border-left"> """
                                    unit :+= """            <img src=" """ + item._3.imageURL + "\""
                                    unit :+= """                 class="rounded" """
                                    unit :+= """                 width="160" height="160" """
                                    unit :+= """                 style="margin:20px 5px; object-fit:cover"/> """
                                    unit :+= """        </div> """
                                    unit :+= """        <div class="col-md-4 text-dark"> """
                                    unit :+= """            <h2 class="text-center">""" + item._3.name + "</h2>"
                                    unit :+= """        </div> """
                                    unit :+= """    </div> """
                                    if (item._1 % 10 == 0)
                                      unit :+= "    <div style=\"page-break-before:always\" ></div>"
                                    unit
                                  }
                     appendedSeq <- IO(rSeq ++ renderUnit)
                   } yield appendedSeq
               }
    } yield (result :+ "</div>").mkString("\n")

  private[this] def element2Participants(
      input: Seq[(ParticipantStatus, Elements)]): IO[Seq[ConnpassParticipant]] =
    for {
      result <- input.foldLeft(IO(Seq[ConnpassParticipant]())) { (init, items) =>
                 for {
                   initSeq <- init
                   users <- element2Users(items._2)
                   participants <- IO {
                                    users
                                      .toArray(Array[Element]())
                                      .map {
                                        var userCounter = 0
                                        user =>
                                          {
                                            val displayName = user
                                              .select("p.display_name a")
                                              .text()
                                            val userHome =
                                              try Jsoup
                                                .connect(
                                                  user
                                                    .select("p.display_name a")
                                                    .attr("href"))
                                                .get()
                                              catch {
                                                case e: IOException =>
                                                  throw GodfatherRendererException(e.getMessage)
                                              }
                                            val images = userHome.select(
                                              "div[id=side_area] div[class=mb_20 text_center] a.image_link")
                                            val imageSource =
                                              if (!images.isEmpty)
                                                images
                                                  .toArray(Array[Element]())
                                                  .find(_.attr("href")
                                                    .contains("/user/"))
                                                  .map(image => new URL(image.attr("href")))
                                                  .getOrElse(IMAGE_SOURCE_DEFAULT)
                                              else IMAGE_SOURCE_DEFAULT

                                            userCounter += 1
                                            logger.info(
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
                   appendedInitSeq <- IO(initSeq ++ participants)
                 } yield appendedInitSeq
               }
    } yield result

  private[this] def element2Users(elems: Elements): IO[Elements] =
    for {
      result <- elems
                 .toArray(Array[Element]())
                 .foldLeft(IO.pure(new Elements())) { (init, item) =>
                   for {
                     initElems <- init
                     _ <- IO {
                           val paginatedUserListLink = item
                             .select("tr.empty td[colspan=2] a")
                           if (paginatedUserListLink.isEmpty)
                             initElems.add(item)
                           else {
                             val paginatedUserListUrl =
                               paginatedUserListLink
                                 .first()
                                 .attr("href")
                             if (paginatedUserListUrl == null || !paginatedUserListUrl
                                   .contains("/ptype/"))
                               initElems.add(item)
                             else {
                               val page1 =
                                 try Jsoup
                                   .connect(paginatedUserListUrl)
                                   .get()
                                 catch {
                                   case e: IOException =>
                                     throw GodfatherRendererException(e.getMessage)
                                 }
                               initElems.add(page1)
                               val participantsCount = page1
                                 .select("span.participants_count")
                                 .text()
                                 .replace("人", "")
                                 .toInt
                               val lastPage = participantsCount / 100 + 1
                               var i = 2
                               while (i <= lastPage) {
                                 val pageX =
                                   try Jsoup
                                     .connect(paginatedUserListUrl + "?page=" + i)
                                     .get()
                                   catch {
                                     case e: IOException =>
                                       throw GodfatherRendererException(e.getMessage)
                                   }
                                 initElems.add(pageX)
                                 i += 1
                               }
                             }
                           }
                         }
                   } yield initElems
                 }
      users <- IO.pure(result.select("td.user .user_info"))
    } yield users
}
