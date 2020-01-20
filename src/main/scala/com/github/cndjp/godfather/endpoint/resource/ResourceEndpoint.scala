package com.github.cndjp.godfather.endpoint.resource

import java.net.URL

import cats.effect.{ContextShift, IO}
import io.finch.Endpoint

import scala.concurrent.ExecutionContext

class ResourceEndpoint {
  implicit val S: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def createContentHTML =
    Endpoint[IO].classpathAsset("/cards.html") :+: Endpoint[IO].classpathAsset("/index.html")

  def createContentJS = Endpoint[IO].classpathAsset("/include.js")
}
