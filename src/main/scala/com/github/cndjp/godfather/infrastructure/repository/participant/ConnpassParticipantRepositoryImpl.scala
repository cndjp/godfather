package com.github.cndjp.godfather.infrastructure.repository.participant

import java.io.IOException
import java.net.URL

import cats.implicits._
import cats.effect.IO
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.domain.user_elements.UserElements
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherRendererException
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ConnpassParticipantRepositoryImpl(scrapeAdapter: ScrapeAdapter)
    extends ConnpassParticipantRepository
    with LazyLogging {
  // HTMLのエレメントから、登録者リストを返す
  override def element2Participants(
      input: Seq[(ParticipantStatus, Elements)]): IO[Seq[ConnpassParticipant]] =
    for {
      result <- input.foldLeft(IO.pure(Seq.empty[ConnpassParticipant])) { (all, items) =>
                 for {
                   allSeq <- all
                   users <- element2Users(items._2)
                   participants <- users.elems
                                    .toArray(Array[Element]())
                                    .foldLeft(IO.pure(Seq.empty[ConnpassParticipant])) {
                                      var userCounter = 0
                                      (unit, elem) =>
                                        for {
                                          unitSeq <- unit
                                          displayName <- IO(elem.select("p.display_name a").text())
                                          userDoc <- scrapeAdapter
                                                      .getDocument(
                                                        elem
                                                          .select("p.display_name a")
                                                          .attr("href"))
                                                      .flatMap {
                                                        case Right(doc) => IO.pure(doc)
                                                        case Left(e) =>
                                                          IO.raiseError(GodfatherRendererException(
                                                            e.getMessage))
                                                      }
                                          participant <- IO(
                                                          ConnpassParticipant(displayName, userDoc))
                                          appendedUnitSeq <- IO(userCounter += 1) *>
                                                              IO(logger.info(
                                                                s"${participant.name} (${items._1.getName}): $userCounter / ${users.elems
                                                                  .size()}")) *>
                                                              IO(unitSeq :+ participant)
                                        } yield appendedUnitSeq
                                    }
                   appendedAllSeq <- IO(allSeq ++ participants)
                 } yield appendedAllSeq
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
                    .foldLeft(Seq.empty[(Int, ConnpassParticipant, ConnpassParticipant)]) {
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
                     paginatedUserListLink <- IO(item.select("tr.empty td[colspan=2] a"))
                     _ <- if (paginatedUserListLink.isEmpty)
                           IO(initElems.add(item))
                         else {
                           val paginatedUserListUrl =
                             paginatedUserListLink
                               .first()
                               .attr("href")
                           if (paginatedUserListUrl == null || !paginatedUserListUrl
                                 .contains("/ptype/"))
                             IO(initElems.add(item))
                           else {
                             var i = 2
                             for {
                               page1 <- scrapeAdapter
                                         .getDocument(paginatedUserListUrl)
                                         .flatMap {
                                           case Right(doc) => IO.pure(doc)
                                           case Left(e) =>
                                             IO.raiseError(GodfatherRendererException(e.getMessage))
                                         }
                               _ <- IO(initElems.add(page1))
                               participantsCount = page1
                                 .select("span.participants_count")
                                 .text()
                                 .replace("人", "")
                                 .toInt
                               lastPage = participantsCount / 100 + 1
                               _ <- IO {
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
                             } yield ()
                           }
                         }
                   } yield initElems
                 }
      users <- IO.pure(result.select("td.user .user_info"))
    } yield UserElements(users)
}
