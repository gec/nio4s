package net.agileautomata.executor4s

import java.util.concurrent.CountDownLatch

/**
 * Functional transformation routines on collections of futures
 */
object Futures {

  /**
   * Turns a collection of futures into a single future
   */
  def gather[A](t: Seq[Future[A]]): Future[Seq[A]] = gatherMap(t)(x => x)

  def gatherMap[A,B](t: Seq[Future[A]])(convert: A => B): Future[Seq[B]] = t.headOption match {
    case Some(head) =>
      val f = head.replicate[Seq[B]]
      val size = t.size
      val map  = collection.mutable.Map.empty[Int, A]

      def gather(i: Int)(a: A) = map.synchronized {
        map.put(i, a)
        if(map.size == size) f.set(t.indices.map(i => convert(map(i))))
      }

      t.zipWithIndex.foreach { case (f, i) => f.listen(gather(i)) }
      f

    case None =>
      throw new IllegalArgumentException("Collect cannot be applied to empty collection")
  }

}