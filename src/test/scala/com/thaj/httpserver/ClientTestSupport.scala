package com.thaj.httpserver

import java.io._
import java.net.Socket

import scala.util.Try

trait ClientTestSupport {
  def getSocket(hostName: String, portNumber: Int): Try[Socket] =
    Try {
      new Socket(hostName, portNumber)
    }

  def writeToSocketOutputStream(request: String, socket: Socket): Try[Unit] = {
    // For the time being the effect is handled using scala's Try. Ideally we can define an IO Monad and use
    // it to make the program effectful.
    Try {
      val out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream))
      out.println(request)
    }
  }

  def readFromSocket(socket: Socket): Try[String]= {
    Try {
      val inputFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val response = inputFromSocket.readLine()
      response
    }
  }
}
