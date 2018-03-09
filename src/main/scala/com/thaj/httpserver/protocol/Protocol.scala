package com.thaj.httpserver.protocol

import com.thaj.httpserver.functional.syntax.Kleisli.ReaderT
import com.thaj.httpserver.requesttoresponse.RequestHandler.{ HttpRequest, HttpResponse }
import scala.util.Try

//
// Protocol is responsible only for validating and converting the input string as Http Request,
// and converting HttpResponse to Response String
//
trait Protocol[F[_], Input, Request, Response, Output] {
  def inputToRequest: ReaderT[F, Input, Request]
  def responseToOutput: ReaderT[F, Response, Output]
}

object Protocol {
  case object HttpProtocol extends Protocol[Try, String, HttpRequest, HttpResponse, String] {
    def inputToRequest: ReaderT[Try, String, HttpRequest] =
      // Convert a RequestString to HttpRequest and that will be a trait
      ReaderT { r => Try { r } }

    def responseToOutput: ReaderT[Try, HttpResponse, String] =
      // For the time being
      ReaderT { x => Try { s"HTTP/1.1 ${x.status.code} OK" } }
  }
}
