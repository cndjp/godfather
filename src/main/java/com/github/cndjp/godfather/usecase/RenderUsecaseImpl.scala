package com.github.cndjp.godfather.usecase
import better.files.File
import cats.effect.IO
import cats.syntax.all._
import com.github.cndjp.godfather.domain.event.ConnpassEvent
import com.github.cndjp.godfather.domain.participant.{
  ConnpassParticipant,
  Participant,
  ParticipantStatus
}
import com.github.cndjp.godfather.domain.repository.ConnpassEventRepository

class RenderUsecaseImpl(connpassEventRepository: ConnpassEventRepository) extends RenderUsecase {
  override def render(event: ConnpassEvent): IO[String] =
    for {
      //tmpFile <- IO(File.newTemporaryFile(suffix = "html"))
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
      //file <- IO(tmpFile.write(participantList2String(title, checkedParticipants))) *> IO(tmpFile.)
      output <- IO(participantList2String(title, checkedParticipants))
    } yield output

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
      result :+= s"            <img src=${input(counter).imageURL}"
      result :+= """                 class="rounded" """
      result :+= """                 width="160" height="160" """
      result :+= """                 style="margin:20px 5px; object-fit:cover"/> """
      result :+= """        </div> """
      result :+= """        <div class="col-md-4 text-dark"> """
      result :+= """            <h2 class="text-center">""" + input(counter).name + "</h2>"
      result :+= """        </div> """
      result :+= """        <div class="col-md-2 border-left" """
      result :+= s"            <img src=${input(counter).imageURL}"
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
    result.mkString("""\n""")
  }
}
