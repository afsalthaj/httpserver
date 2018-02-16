package com.thaj.httpserver.socket

// All that we need is a server that accepts requests through a socket.
// All that makes it an httpserver is that it excepts some protocol in the communication - http!
trait HttpSocket {
  val portNumber = 8080


}
