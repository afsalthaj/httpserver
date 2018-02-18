package com.thaj.httpserver.protocol

// Ideally we expect HttpProtocol[A], parameterised by a parser
trait HttpProtocol {
  def asInput: String => HttpProtocol.Request
  // some bits and pieces of http protocol
  def processInput(input: HttpProtocol.Request): HttpProtocol.Response
  // For the time being we convert to String for sending out response
  def asOutput: HttpProtocol.Response => String
}

object HttpProtocol {
  val StatusOK: Int = 200
  type Request = String
  case class Response(status: Int, body: Option[String])
  // Easy to add a new http like protocol for testing
  case object StandardProtocol extends HttpProtocol {
    def processInput(input: Request): Response =
    // Ex: input = "GET / HTTP/1.1"
    // parseInput match {
    // case Method.GET => Response(Status.Ok....bla)
      Response(StatusOK, None)

    def asInput: (String) => Request =
      identity

    def asOutput: (Response) => String =
      // For the time being
      _ => "HTTP/1.1 200 OK"
  }
}
