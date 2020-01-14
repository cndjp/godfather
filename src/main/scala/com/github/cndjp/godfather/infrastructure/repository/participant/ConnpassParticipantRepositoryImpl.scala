package com.github.cndjp.godfather.infrastructure.repository.participant

import java.io.IOException
import java.net.URL
import java.util.UUID
import cats.implicits._
import cats.effect.IO
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.domain.user_elements.UserElements
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherRendererException
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ConnpassParticipantRepositoryImpl extends ConnpassParticipantRepository with LazyLogging {
  private[this] val IMAGE_SOURCE_DEFAULT = new URL(
    "https://connpass.com/static/img/common/user_no_image_180.png")

  // HTMLのエレメントから、登録者リストを返す
  override def element2Participants(
      input: Seq[(ParticipantStatus, Elements)]): IO[Seq[ConnpassParticipant]] =
    for {
      result <- input.foldLeft(IO(Seq[ConnpassParticipant]())) { (init, items) =>
                 for {
                   initSeq <- init
                   users <- element2Users(items._2)
                   participants <- users.elems
                                    .toArray(Array[Element]())
                                    .foldLeft(IO.pure(Seq[ConnpassParticipant]())) {
                                      var userCounter = 0
                                      (init, elem) =>
                                        for {
                                          initSeq <- init
                                          displayName <- IO(elem.select("p.display_name a").text())
                                          userDoc <- try IO(
                                                      Jsoup
                                                        .connect(elem
                                                          .select("p.display_name a")
                                                          .attr("href"))
                                                        .get())
                                                    catch {
                                                      case e: IOException =>
                                                        IO.raiseError(
                                                          GodfatherRendererException(e.getMessage))
                                                    }
                                          participant <- IO(
                                                          ConnpassParticipant(displayName, userDoc))
                                          appendedSeq <- IO(userCounter += 1) *>
                                                          IO(logger.info(
                                                            s"${participant.name} (${items._1.getName}): $userCounter / ${users.elems
                                                              .size()}")) *>
                                                          IO(initSeq :+ participant)
                                        } yield appendedSeq
                                    }
                   appendedInitSeq <- IO(initSeq ++ participants)
                 } yield appendedInitSeq
               }
    } yield result

  // 登録者リストをパースしてcards.htmlに書き込むHTMLの文字列を返す
  override def parseParticipantList(title: String, input: Seq[ConnpassParticipant]): IO[String] =
    for {
      adjust <- IO {
                 if (input.size % 2 == 1)
                   input :+ ConnpassParticipant("", "", null)
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

  // connpassのURLからfetchしてきたHTMLエレメントを加工して、利用しやすい形の登録者全員のHTMLにして返す
  private[this] def element2Users(elems: Elements): IO[UserElements] =
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
    } yield UserElements(users)
}
