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
package net.agileautomata.executor4s.testing

import net.agileautomata.executor4s._
import collection.mutable.Queue
import collection.mutable.Set
import annotation.tailrec

final class MockExecutor(val recursionLimit: Int = 1000) extends Strand {

  private var timeNanoSec: Long = 0

  private case class Action(fun: () => Unit)

  private case class Timer(fun: () => Unit, expiration: Long, var canceled: Boolean = false) extends Cancelable {
    def cancel() = canceled = true
  }

  private implicit object TimerOrdering extends Ordering[Timer] {
    def compare(x: Timer, y: Timer) = (x.expiration - y.expiration).toInt
  }

  private val actions = Queue.empty[Action]
  private val timers = Set.empty[Timer]

  def execute(fun: => Unit): Unit = actions.enqueue(Action(() => fun))

  def delay(interval: TimeInterval)(fun: => Unit): Cancelable = {
    val expiration = timeNanoSec + interval.nanosec
    val timer = Timer(() => fun, expiration)
    timers += timer
    timer
  }

  def attempt[A](fun: => A): Future[Result[A]] = {
    val f = MockFuture.undefined[Result[A]]
    execute(f.set(Result(fun)))
    f
  }

  def runNextPendingAction(): Boolean = actions.headOption match {
    case Some(x) =>
      actions.dequeue()
      x.fun()
      true
    case None =>
      false
  }

  /**
   * Runs queue execute tasks until the queue is empty
   */
  def runUntilIdle(): Unit = {
    @tailrec
    def inner(count: Int): Unit = actions.headOption match {
      case Some(x) =>
        if (count >= recursionLimit) throw new Exception("Recursion limit reached in iteration: " + count)
        actions.dequeue()
        x.fun()
        inner(count + 1)
      case None =>
    }
    inner(1)
  }

  /**
   * Execute all pending actions and any timers within the interval
   */
  def tick(interval: TimeInterval) = {
    timeNanoSec += interval.nanosec
    @tailrec
    def inner(count: Int): Unit = {
      if (count >= recursionLimit) throw new Exception("Recursion limit reached in iteration: " + count)
      runUntilIdle()
      if (!timers.isEmpty) {
        val t = timers.min
        if (timeNanoSec >= t.expiration) {
          t.fun()
          timers.remove(t)
          inner(count + 1)
        }
      }
    }
    inner(1)
  }
}