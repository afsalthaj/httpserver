package com.thaj.httpserver.repository

/**
 * Created by afsalthaj on 3/8/18.
 */
trait Repository[F[_], K, V] {
  def write(k: K, v: V): F[Unit]
  def read(k: K): F[V]
  def delete(k: K): F[Unit]
}
