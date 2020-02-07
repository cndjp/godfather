package com.github.cndjp.godfather.infrastructure.repository.participant

import cats.effect.IO
import com.github.cndjp.godfather.domain.cards.RenderedCards
import com.github.cndjp.godfather.domain.elements.participants.ParticipantsElements
import com.github.cndjp.godfather.domain.event.ConnpassTitle
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.participant.ConnpassParticipantRepository
import com.github.cndjp.godfather.infrastructure.adapter.scrape.ScrapeAdapter
import com.typesafe.scalalogging.LazyLogging
import org.jsoup.nodes.Element
import com.github.cndjp.godfather.utils.GodfatherDefaultObject._

class ConnpassParticipantRepositoryImpl(scrapeAdapter: ScrapeAdapter)
    extends ConnpassParticipantRepository
    with LazyLogging {
  // HTMLのエレメントから、登録者リストを返す
  override def element2Participant(input: ParticipantsElements): IO[Seq[ConnpassParticipant]] =
    for {
      participants <- input.elems
                       .toArray(Array[Element]())
                       .foldLeft(IO.pure(Seq.empty[ConnpassParticipant])) {
                         var userCounter = 0
                         (unit, elem) =>
                           for {
                             unitSeq <- unit
                             displayName <- IO(elem.select("p.display_name a").text())
                             maybeUserDoc <- scrapeAdapter
                                              .getDocument(
                                                elem
                                                  .select("p.display_name a")
                                                  .attr("href"))
                             appendedUnitSeq <- maybeUserDoc.fold(
                                                 e =>
                                                   for {
                                                     _ <- IO(userCounter += 1)
                                                     _ <- IO(logger.error(e.getMessage))
                                                   } yield unitSeq,
                                                 userDoc =>
                                                   for {
                                                     participant <- IO(
                                                                     ConnpassParticipant(
                                                                       displayName,
                                                                       userDoc))
                                                     _ <- IO(userCounter += 1)
                                                     _ <- IO(logger.info(
                                                           s"${participant.name}: $userCounter / ${input.elems
                                                             .size()}"))
                                                   } yield unitSeq :+ participant
                                               )
                           } yield appendedUnitSeq
                       }
    } yield participants

  // 登録者リストをレンダリングしてcards.htmlに書き込むHTMLの文字列を返す
  override def renderParticipantList(title: ConnpassTitle,
                                     input: Seq[ConnpassParticipant]): IO[RenderedCards] =
    for {
      adjust <- IO {
                 if (input.size % 2 == 1)
                   input :+ ConnpassParticipant("unknown", "blank", IMAGE_SOURCE_DEFAULT)
                 else input
               }
      factory <- IO {
                  (0 to (input.size / 2))
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
                                    unit :+= """            <h4 class="text-center">""" + title.value + "</h4>"
                                    unit :+= """        </div> """
                                    unit :+= """        <div class="col-md-6 py-2 bg-info text-light border-left"> """
                                    unit :+= """            <h4 class="text-center">""" + title.value + "</h4>"
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
    } yield RenderedCards((result :+ "</div>").mkString("\n"))
}
