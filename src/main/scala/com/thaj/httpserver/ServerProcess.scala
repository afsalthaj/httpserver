package com.thaj.httpserver

trait ServerProcess[A]

object ServerProcess {
  // I don't know, may be, may be not... I see myself writing a Free.. I need it I guess.. Let's see.
  case class Request(s: String) extends ServerProcess[String]
  case class Response(v: String) extends ServerProcess[String]
}
