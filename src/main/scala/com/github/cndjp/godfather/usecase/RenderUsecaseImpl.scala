package com.github.cndjp.godfather.usecase
import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import cats.effect.IO
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{ConnpassParticipant, ParticipantStatus}
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository

import scala.io.Source

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository) extends RenderUsecase {
  private[this] val resourcesPath = "./src/main/resources"

  override def exec(event: ConnpassEvent): IO[String] =
    for {
      cardHTMLPath <- IO(Paths.get(s"$resourcesPath/cards.html"))
      _ <- if (Files.exists(cardHTMLPath)) IO.unit else render(event, cardHTMLPath)
      indexHTML <- IO {
                    val indexBuf = Source.fromFile(s"$resourcesPath/index.html")
                    try indexBuf.mkString
                    finally indexBuf.close()
                  }
    } yield indexHTML

  private[this] def render(event: ConnpassEvent, cardHTMLPath: Path): IO[Unit] =
    for {
      cardHTML <- IO { Files.createFile(cardHTMLPath) }
      participants <- connpassEventRepository.getParticipants(event)
      checkedParticipants <- IO.pure {
                              if (participants.size % 2 == 1) {
                                participants :+ ConnpassParticipant(
                                  "",
                                  "",
                                  null,
                                  ParticipantStatus.CANCELLED);
                              } else participants
                            }
      title <- connpassEventRepository.getEventTitle(event)
      output <- IO(participantList2String(title, checkedParticipants))
      _ <- IO {
            val pw = new PrintWriter(cardHTML.toFile.getPath)
            try pw.write(output)
            finally pw.close()
          }
    } yield ()

  private[this] def participantList2String(title: String,
                                           input: Seq[ConnpassParticipant]): String = {
    var result = Seq[String]()
    var counter = 0
    result :+= """<div class="container border">"""
    while (counter < input.size) {
      result :+= """    <div class="row align-items-center border"> """
      result :+= """        <div class="col-md-6 py-2 bg-info text-light"> """
      result :+= """            <h4 class="text-center">""" + title + "</h4>"
      result :+= """        </div> """
      result :+= """        <div class="col-md-6 py-2 bg-info text-light border-left"> """
      result :+= """            <h4 class="text-center">""" + title + "</h4>"
      result :+= """        </div> """
      result :+= """    </div> """
      result :+= """    <div class="row align-items-center border"> """
      result :+= """        <div class="col-md-2"> """
      result :+= """            <img src=" """ + input(counter).imageURL + "\""
      result :+= """                 class="rounded" """
      result :+= """                 width="160" height="160" """
      result :+= """                 style="margin:20px 5px; object-fit:cover"/> """
      result :+= """        </div> """
      result :+= """        <div class="col-md-4 text-dark"> """
      result :+= """            <h2 class="text-center">""" + input(counter).name + "</h2>"
      result :+= """        </div> """
      result :+= """        <div class="col-md-2 border-left"> """
      result :+= """            <img src=" """ + input(counter).imageURL + "\""
      result :+= """                 class="rounded" """
      result :+= """                 width="160" height="160" """
      result :+= """                 style="margin:20px 5px; object-fit:cover"/> """
      result :+= """        </div> """
      result :+= """        <div class="col-md-4 text-dark"> """
      result :+= """            <h2 class="text-center">""" + input(counter).name + "</h2>"
      result :+= """        </div> """
      result :+= """    </div> """
      if ((counter + 2) % 10 == 0) {
        result :+= "    <div style=\"page-break-before:always\" />"
      }
      counter += 2
    }
    result :+= "</div>"
    result.mkString("\n")
  }
}
