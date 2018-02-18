package com.thaj.httpserver

import java.io._
import java.net.Socket

import com.thaj.httpserver.server.HttpServer

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import scala.concurrent.ExecutionContext.Implicits.global

trait ClientTestSupport {
  val testPort = 8888
  private def getSocket(hostName: String, portNumber: Int): Try[Socket] =
    Try {
      new Socket(hostName, portNumber)
    }

  private def writeToSocket(request: String, socket: Socket): Try[Unit] = {
    // For the time being the effect is handled using scala's Try. Ideally we can define an IO Monad and use
    // it to make the program effectful.
    Try {
      val out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream), true)
      // scalastyle: off
      out.println(request)
    }
  }

  private def readFromSocket(socket: Socket): Try[String] = {
    Try {
      val inputFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val y = inputFromSocket.readLine()
      y
    }
  }

  // The evil of future.. Future is just future, not lazy
  val asyncServer = Future {
    HttpServer.run(testPort) match {
      case Success(x) => x
      case Failure(y) => throw new Exception(y)
    }
  }

  def connectAndSendRequest(hostName: String, portNumber: Int, request: String): Try[String] = {
    for {
      socket <- getSocket(hostName, portNumber)
      _ <- writeToSocket(request, socket)
      response <- readFromSocket(socket)
      // Evil of future
      _ = asyncServer.onComplete(_.foreach(_.close()))
    } yield response
  }

}
