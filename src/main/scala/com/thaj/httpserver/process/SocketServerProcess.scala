package com.thaj.httpserver.process

import java.io.{ BufferedReader, InputStreamReader, PrintWriter }
import java.net.{ ServerSocket, Socket }

import com.thaj.httpserver.protocol.Protocol
import scala.util.Try

// To start with, to represent effects, we use Try.
// This could be an IO... may be
// This is more or less divided to get an overview of the functionalities we expect from Socket/ServerSocket
// TODO; investiagate scala socket programming, as we find functions accepting `Object` and being indeterministic.
// Ideally I expect this to be SocketServerProcess[A <: HttpProtocol], parameterised by HttpProtocol
trait SocketServerProcess[Request, Response] {
  val protocol: Protocol[String, Request, Response]

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

  private def processInputFromSocket(reader: SocketReader): Try[Response] =
    Try {
      reader.readLine
    }.map(protocol.asInput andThen protocol.processInput)

  def readFromSocketAndProcess(clientSocket: Socket): Try[Response] =
    readFromSocket(clientSocket).flatMap(processInputFromSocket)

  def writeToSocket(clientSocket: Socket, response: Response): Try[Unit] =
    Try {
      // scalastyle:off
      new PrintWriter(clientSocket.getOutputStream, true).println(protocol.asOutput(response))
    }
}
