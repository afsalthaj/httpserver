package com.thaj.httpserver.socketserverprocess

import java.io.{ BufferedReader, InputStreamReader, PrintWriter }
import java.net.{ ServerSocket, Socket }

import com.thaj.httpserver.functional.syntax.Kleisli
import com.thaj.httpserver.functional.syntax.Kleisli.ReaderT
import com.thaj.httpserver.protocol.Protocol
import com.thaj.httpserver.repository.Repository
import scala.util.Try

trait SocketServerProcess[Request, Response] { self =>
  // TODO; Fix types with Repository. It neednt be concrete. Currently placed as we bumped into a few type issues
  type RequestHandler = ReaderT[Try, (Request, Repository[Try, String, String]), Response]

  def requestHandler: RequestHandler
  def protocol: Protocol[Try, String, Request, Response, String]

  type SocketReader = BufferedReader

  def newServerSocket: ReaderT[Try, Int, ServerSocket] =
    Kleisli {
      n => Try { new ServerSocket(n) }
    }

  def newClientSocket: ReaderT[Try, ServerSocket, Socket] =
    Kleisli {
      s => Try { s.accept() }
    }

  private def readFromSocket: ReaderT[Try, Socket, SocketReader] =
    Kleisli {
      s =>
        Try {
          new BufferedReader(new InputStreamReader(s.getInputStream))
        }
    }

  private def processInputFromSocket: Repository[Try, String, String] => ReaderT[Try, SocketReader, Response] = {
    (r: Repository[Try, String, String]) =>
      Kleisli[Try, SocketReader, String](reader => Try {
        reader.readLine()
      })
        .>=>(Kleisli[Try, String, (Request, Repository[Try, String, String])](protocol.inputToRequest.run(_).map(req => (req, r))))
        .>=>[Response](Kleisli { case (r1, r2) => requestHandler.run(r1, r2) })
  }

  def readFromSocketAndProcess: Repository[Try, String, String] => ReaderT[Try, Socket, Response] = {
    (r: Repository[Try, String, String]) =>
      readFromSocket.>=>(processInputFromSocket(r))
  }

  def writeToSocket(clientSocket: Socket): ReaderT[Try, Response, Unit] =
    Kleisli(response => {
      // scalastyle:off
      protocol.responseToOutput.run(response).map(new PrintWriter(clientSocket.getOutputStream, true).println)
    })
}
