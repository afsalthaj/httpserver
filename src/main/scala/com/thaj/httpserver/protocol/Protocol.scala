package com.thaj.httpserver.protocol

// Ideally we expect HttpProtocol[A], parameterised by a parser
trait Protocol[Input, Request, Response] {
  def asInput: Input => Request
  // some bits and pieces of http protocol
  def processInput(input: Request): Response
  // For the time being we convert to String for sending out response
  def asOutput: Response => Input
}

object Protocol {
  val StatusOK: Int = 200
  val StatusFail: Int = 401
  type HttpRequest = String
  case class HttpResponse(status: Int, body: Option[String])
  // Easy to add a new http like protocol for testing
  case object HttpProtocol extends Protocol[String, HttpRequest, HttpResponse] {
    def processInput(input: HttpRequest): HttpResponse = {
      // Ex: input = "GET / HTTP/1.1"
      // parseInput match {
      // case Method.GET => Response(Status.Ok...)
      val inputs = input.split(" ")
      if (inputs.nonEmpty && inputs.toList(1) == "/") {
        HttpResponse(StatusOK, None)
      } else {
        HttpResponse(StatusFail, None)
      }
    }

    def asInput: (String) => HttpRequest =
      identity

    def asOutput: (HttpResponse) => String =
      // For the time being
      x => x.status match {
        case StatusOK => "HTTP/1.1 200 OK"
        case StatusFail => "HTTP/1.1 404 OK"
      }
  }
}
