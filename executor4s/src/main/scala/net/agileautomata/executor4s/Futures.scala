/**
 * Copyright 2011 J Adam Crain (jadamcrain@gmail.com)
 *
 * Licensed to J Adam Crain under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. J Adam Crain licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

  def gatherMap[A, B](t: Seq[Future[A]])(convert: A => B): Future[Seq[B]] = t.headOption match {
    case Some(head) =>
      val f = head.replicate[Seq[B]]
      val size = t.size
      val map = collection.mutable.Map.empty[Int, A]

      def gather(i: Int)(a: A) = map.synchronized {
        map.put(i, a)
        if (map.size == size) f.set(t.indices.map(i => convert(map(i))))
      }

      t.zipWithIndex.foreach { case (f, i) => f.listen(gather(i)) }
      f

    case None =>
      throw new IllegalArgumentException("Collect cannot be applied to empty collection")
  }

}