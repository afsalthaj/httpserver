package com.thaj.httpserver.requesttoresponse

import scala.util.Failure
import scala.util.Try

import com.thaj.httpserver.repository.Repository
import com.thaj.httpserver.functional.syntax.Kleisli, Kleisli.ReaderT

trait RequestHandler[F[_], Request, Repository, Response] {
  def processRequest: ReaderT[F, (Request, Repository), Response]
}

object RequestHandler {
  // Move HttpRequest type and HttpResponse outside, may be in protocol
  type HttpRequest = String

  case class HttpResponse(status: HttpResponse.Status, body: Option[String])

  object HttpResponse {
    abstract sealed class Status(val code: Int)
    // scalastyle:off magic.number
    case object StatusOk extends Status(200)
    case object StatusFailure extends Status(404)
  }

  // TODO:We neednt have Repository to have Try, String, String. It is just that I bumped into type alias and lamba issues in scala.
  case object HttpRequestHandler extends RequestHandler[Try, HttpRequest, Repository[Try, String, String], HttpResponse] {
    override def processRequest: ReaderT[Try, (HttpRequest, Repository[Try, String, String]), HttpResponse] = {
      Kleisli {
        case (request, repository) => {
          // This is an dirty version to get the cobspec pass in the first instance.
          // TODO; once request is already parsed, it should tell us what is its METHOD, what is the body and key value pairs
          val inputs = request.split(" ")
          if (inputs(0) == "GET") {
            get.run(request)
          } else if (inputs(0) == "POST") {
            post.run(request, repository)
          } else if (inputs(0) == "PUT") {
            put.run(request, repository)
          } else {
            Failure(throw new Exception("Unsupported Http Method"))
          }
        }
      }
    }

    private def get: ReaderT[Try, HttpRequest, HttpResponse] = {
      Kleisli { request =>
        {
          // TODO; once request is already parsed, it should tell us what is its METHOD, what is the body and key value pairs
          val inputs = request.split(" ")
          if (inputs.nonEmpty && inputs.toList(1) == "/") {
            Try {
              HttpResponse(HttpResponse.StatusOk, None)
            }
          } else {
            Try {
              HttpResponse(HttpResponse.StatusFailure, None)
            }
          }
        }
      }
    }

    private def post: ReaderT[Try, (HttpRequest, Repository[Try, String, String]), HttpResponse] = {
      Kleisli {
        case (request, repository) => {
          // request will be a parsed request based on protocol defined.
          repository.write("My", "Data").map(_ => HttpResponse(HttpResponse.StatusOk, None))
        }
      }
    }

    private def put: ReaderT[Try, (HttpRequest, Repository[Try, String, String]), HttpResponse] = {
      Kleisli {
        case (request, repository) => {
          // TODO; once request is already parsed, it should tell us what is its METHOD, what is the body and key value pairs
          for {
            // TODO; Complete parser
            _ <- repository.read("My")
            _ <- repository.write("My", "Data")
          } yield HttpResponse(HttpResponse.StatusOk, None)
        }
      }
    }
  }
}