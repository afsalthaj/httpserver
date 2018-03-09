package com.thaj.httpserver.functional

import scala.util.Try

package object syntax {
  implicit val TryFunctor = new Functor[Try] {
    override def map[A, B](a: Try[A])(f: (A) => B): Try[B] =
      a.map(f)
  }

  implicit val TryBind = new Bind[Try] {
    override def bind[A, B](a: Try[A])(f: (A) => Try[B]): Try[B] =
      a.flatMap(f)
  }
}
