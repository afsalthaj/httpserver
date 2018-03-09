package com.thaj.httpserver.server

import scala.util.{ Failure, Success, Try }

import java.net.ServerSocket
import com.thaj.httpserver.socketserverprocess.SocketServerProcess
import com.thaj.httpserver.protocol.Protocol
import com.thaj.httpserver.repository.{ InMemoryRepository, Repository }
import com.thaj.httpserver.requesttoresponse.RequestHandler
import com.thaj.httpserver.requesttoresponse.RequestHandler.{ HttpRequest, HttpResponse }

object HttpServer {
  object HttpServerSocketProcess extends SocketServerProcess[HttpRequest, HttpResponse] {
    val protocol = Protocol.HttpProtocol
    val requestHandler = RequestHandler.HttpRequestHandler.processRequest
  }

  import HttpServerSocketProcess._

  // TODO; Avoid running Kleisli in the composition and make the description purely lazy.
  def communicate(serverSocket: ServerSocket, repository: Repository[Try, String, String]): Try[ServerSocket] = {
    for {
      clientSocket <- newClientSocket.run(serverSocket)
      response <- readFromSocketAndProcess(repository).run(clientSocket)
      _ <- writeToSocket(clientSocket).run(response)
      _ <- Try { clientSocket.close() }
    } yield serverSocket
  }

  val repository = new InMemoryRepository
  // Runs an instance and closes off all the resources
  def run(port: Int): Try[ServerSocket] = {
    newServerSocket.run(port) match {
      case Success(ser) => communicate(ser, repository)
      case Failure(y) => throw new Exception("Failed to start the server" + y)
    }
  }

  def init(port: Int): Unit = {
    newServerSocket.run(port) match {
      case Success(ser) => while (true) communicate(ser, repository)
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
