package com.thaj.httpserver.experiment

/**
 * Server Side process
 * Open a socket.
 * Open an input stream and output stream to the socket.
 * Read from and write to the stream according to the server's protocol.
 * Close the streams.
 * Close the socket.
 * Only step 3 differs from client to client, depending on the server. The other steps remain largely the same.
 */
trait ServerProcess[A]

object ServerProcess {
  // I don't know, may be, may be not... I see myself writing a Free.. I need it I guess.. Let's see.
  case class Request(s: String) extends ServerProcess[String]
  case class Response(v: String) extends ServerProcess[String]
}
