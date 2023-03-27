package io.gdashboard

import cats.effect.{Concurrent, Ref}
import cats.syntax.apply._

trait Counters[F[_]] {
  def nextGauge: F[Int]
  def nextStat: F[Int]
  def nextTimeseries: F[Int]
  def nextTable: F[Int]
}

object Counters {
  def apply[F[_]](implicit ev: Counters[F]): Counters[F] = ev

  def make[F[_]: Concurrent]: F[Counters[F]] =
    (Ref.of[F, Int](0), Ref.of[F, Int](0), Ref.of[F, Int](0), Ref.of[F, Int](0)).mapN {
      (gauge, stat, timeseries, table) =>
        new Counters[F] {
          def nextGauge: F[Int]      = gauge.updateAndGet(_ + 1)
          def nextStat: F[Int]       = stat.updateAndGet(_ + 1)
          def nextTimeseries: F[Int] = timeseries.updateAndGet(_ + 1)
          def nextTable: F[Int]      = table.updateAndGet(_ + 1)
        }
    }
}
