package com.thaj.httpserver.functional.syntax

/**
 * Created by afsalthaj on 3/9/18.
 */
trait Bind[F[_]] {
  def bind[A, B](a: F[A])(f: A => F[B]): F[B]
}
