package com.thaj.httpserver.server

import java.net.ServerSocket

import scala.util.{ Failure, Success, Try }

import com.thaj.httpserver.process.SocketServerProcess
import com.thaj.httpserver.protocol.Protocol

object HttpServer {
  object HttpServerSocketServerProcessProcess extends SocketServerProcess[Protocol.HttpRequest, Protocol.HttpResponse] {
    val protocol = Protocol.HttpProtocol
  }
  import HttpServerSocketServerProcessProcess._

  def communicate(serverSocket: ServerSocket): Try[ServerSocket] = {
    for {
      clientSocket <- getNewClientSocket(serverSocket)
      response <- readFromSocketAndProcess(clientSocket)
      _ <- writeToSocket(clientSocket, response)
      _ <- Try { clientSocket.close() }
    } yield serverSocket
  }

  // Runs an instance and closes off all the resources
  def run(port: Int): Try[ServerSocket] = {
    getNewServerSocket(port) match {
      case Success(ser) => communicate(ser)
      case Failure(y) => throw new Exception("Failed to start the server" + y)
    }
  }

  def init(port: Int): Unit = {
    getNewServerSocket(port) match {
      case Success(ser) => while (true) communicate(ser)
      case Failure(y) => throw new Exception("Failed to start the server" + y)
    }
  }
  def main(args: Array[String]): Unit = {
    val args0 = args(0)
    require(args0 == "-p")

    val port = args(1).toInt

    init(port)

  }
}
