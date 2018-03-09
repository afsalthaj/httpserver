package com.thaj.httpserver.functional.syntax

case class Kleisli[M[_], A, B](run: A => M[B]) {
  def map[C](f: B => C)(implicit M: Functor[M]): Kleisli[M, A, C] =
    Kleisli(a => M.map(run(a))(f))
  def >=>[C](k: Kleisli[M, B, C])(implicit b: Bind[M]): Kleisli[M, A, C] =
    Kleisli((a: A) => b.bind(run(a))(k.run))
  def flatMap[C](f: B => Kleisli[M, A, C])(implicit M: Bind[M]): Kleisli[M, A, C] =
    Kleisli((r: A) => M.bind[B, C](run(r))(((b: B) => f(b).run(r))))
}

object Kleisli {
  type ReaderT[M[_], B, C] = Kleisli[M, B, C]

  object ReaderT {
    def apply[M[_], B, C](f: B => M[C]): ReaderT[M, B, C] = Kleisli[M, B, C](f)
  }
}