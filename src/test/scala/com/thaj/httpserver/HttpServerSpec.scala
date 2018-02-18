package com.thaj.httpserver

import scala.util.Success
import org.specs2.Specification
import org.specs2.specification.core.SpecStructure

object HttpServerSpec extends Specification with ClientTestSupport {
  def is: SpecStructure =
    s"""
       |GET calls responds with a 200 $testBasicGet
     """.stripMargin

  val PORT = 8888

  private def testBasicGet = {
    connectAndSendRequest("localhost", PORT, "GET /") must beLike {
      // TODO; parse request and response bodies..
      case Success(x) if x == "HTTP/1.1 200 OK" => ok
    }
  }
}
