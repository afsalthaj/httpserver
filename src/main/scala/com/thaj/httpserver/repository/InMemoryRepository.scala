package com.thaj.httpserver.repository

import scala.util.{ Failure, Try }

class InMemoryRepository extends Repository[Try, String, String] {
  // Avoiding mutable map
  var map: Map[String, String] = Map[String, String]()

  override def write(k: String, v: String): Try[Unit] = {
    Try {
      map = map + (k -> v)
    }.map(_ => ())
  }

  override def read(key: String): Try[String] = {
    map.get(key) match {
      case Some(value) => scala.util.Success(value)
      case None => Failure(throw new Exception(s"No value corresponding to key $key"))
    }
  }

  override def delete(k: String): Try[Unit] = {
    Try { map = map.-(k) }
  }
}
