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
   * Turns a collection of futures into a single future dispatched on the specified executor
   */
  def gather[A](exe: Executor, futures: Seq[Future[A]]): Future[Seq[A]] = gatherMap(exe, futures)(x => x)

  /**
   * Gathers a sequence of futures into an aggregate future, transforming their values, and dispatching the result
   *  on the specified executor
   */
  def gatherMap[A, B](exe: Executor, futures: Seq[Future[A]])(convert: A => B): Future[Seq[B]] = {

    val f = exe.future[Seq[B]]
    val size = futures.size
    val map = collection.mutable.Map.empty[Int, A]

    def gather(i: Int)(a: A) = map.synchronized {
      map.put(i, a)
      if (map.size == size) f.set(futures.indices.map(i => convert(map(i))))
    }

    if (futures.isEmpty) f.set(Nil)
    else futures.zipWithIndex.foreach { case (f, i) => f.listen(gather(i)) }
    f
  }

  def combine[A,B,C](fa: Future[A], fb: Future[B])(join: (A,B) => C): Future[C] = for(i <- fa; j <- fb) yield join(i,j)

}