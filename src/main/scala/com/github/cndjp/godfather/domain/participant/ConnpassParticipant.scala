package com.github.cndjp.godfather.domain.participant

import java.net.URL
import java.util.UUID
import org.jsoup.nodes.{Document, Element}
import com.github.cndjp.godfather.utils.GodfatherConstants._

// connpassイベントに参加する人を表すドメインクラス
case class ConnpassParticipant(id: String, name: String, imageURL: URL) extends Participant

object ConnpassParticipant {

  def apply(displayName: String, userDoc: Document): ConnpassParticipant = {
    val images = userDoc.select("div[id=side_area] div[class=mb_20 text_center] a.image_link")
    val imageSource =
      if (!images.isEmpty)
        images
          .toArray(Array[Element]())
          .find(_.attr("href")
            .contains("/user/"))
          .map(image => new URL(image.attr("href")))
          .getOrElse(IMAGE_SOURCE_DEFAULT)
      else IMAGE_SOURCE_DEFAULT
    ConnpassParticipant(
      UUID.randomUUID().toString,
      displayName,
      imageSource,
    )
  }
}
