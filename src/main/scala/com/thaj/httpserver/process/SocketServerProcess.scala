package com.thaj.httpserver.process

import java.io.{ BufferedReader, InputStreamReader, PrintWriter }
import java.net.{ ServerSocket, Socket }

import com.thaj.httpserver.protocol.HttpProtocol
import scala.util.Try

trait SocketServerProcess {
  val protocol: HttpProtocol

  type SocketReader = BufferedReader

  def getNewServerSocket(portNumber: Int): Try[ServerSocket] =
    Try {
      new ServerSocket(portNumber)
    }

  def getNewClientSocket(serverSocket: ServerSocket): Try[Socket] =
    Try {
      serverSocket.accept()
    }

  private def readFromSocket(clientSocket: Socket): Try[SocketReader] =
    Try {
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    }

  private def processInputFromSocket(reader: SocketReader): Try[HttpProtocol.Response] =
    Try {
      val x = reader.readLine
      println("The request is " + x)
      x
    }.map(protocol.asInput andThen protocol.processInput)

  def readFromSocketAndProcess(clientSocket: Socket): Try[HttpProtocol.Response] =
    readFromSocket(clientSocket).flatMap(processInputFromSocket)

  def writeToSocket(clientSocket: Socket, response: HttpProtocol.Response): Try[Unit] =
    Try {
      // scalastyle:off
      new PrintWriter(clientSocket.getOutputStream, true).println(protocol.asOutput(response))
    }
}
