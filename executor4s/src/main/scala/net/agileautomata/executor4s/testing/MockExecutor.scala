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

  private class TimerRecord(var expiration: Long)(fun: => Unit) extends Timer {
    def cancel() = cancelTimer(this)
    def perform() = fun
  }

  private implicit object TimerOrdering extends Ordering[TimerRecord] {
    def compare(x: TimerRecord, y: TimerRecord) = (x.expiration - y.expiration).toInt
  }

  private val actions = Queue.empty[Action]
  private val timers = Set.empty[TimerRecord]

  def execute(fun: => Unit): Unit = actions.enqueue(Action(() => fun))

  def schedule(interval: TimeInterval)(fun: => Unit): Timer = {
    val timer = getTimer(interval)(fun)
    timers += timer
    timer
  }

  def scheduleWithFixedOffset(initial: TimeInterval, offset: TimeInterval)(fun: => Unit): Timer = {

    var t: Option[TimerRecord] = None
    def update(offset: TimeInterval): Unit = t.foreach { x =>
      x.expiration = timeNanoSec + offset.nanosec
      timers += x
    }
    val timer = getTimer(initial) {
      fun
      update(offset)
    }
    t = Some(timer)
    timers += timer
    timer
  }

  private def getTimer(timeout: TimeInterval)(fun: => Unit): TimerRecord =
    new TimerRecord(timeNanoSec + timeout.nanosec)(fun)

  def attempt[A](fun: => A): Future[Result[A]] = {
    val f = MockFuture.undefined[Result[A]]
    execute(f.set(Result(fun)))
    f
  }

  def isIdle = actions.isEmpty

  def numQueuedActions: Int = actions.size
  def numQueuedTimers: Int = timers.size

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

  private def cancelTimer(timer: TimerRecord) = timers.remove(timer)

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
          timers.remove(t)
          t.perform()
          inner(count + 1)
        }
      }
    }
    inner(1)
  }
}