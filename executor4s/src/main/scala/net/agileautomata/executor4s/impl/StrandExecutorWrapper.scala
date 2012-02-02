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
package net.agileautomata.executor4s.impl

import net.agileautomata.executor4s._

private class StrandExecutorWrapper(exe: Executor) extends StrandLifeCycle with Callable {

  def operationTimeout = exe.operationTimeout

  private class Task(val isFinal: Boolean)(fun: => Unit) { def perform() = fun }

  def future[A] = exe.future[A]

  def execute(fun: => Unit): Unit = enqueue(new Task(false)(fun))

  def schedule(interval: TimeInterval)(fun: => Unit): Timer = {
    val timer = new DefaultTimer(operationTimeout)
    val task = new Task(false)(timer.executeIfNotCanceled(fun))
    val t = exe.schedule(interval)(this.enqueue(task))
    timer.onCancel(t.cancel())
    timer
  }

  def scheduleWithFixedOffset(initial: TimeInterval, interval: TimeInterval)(fun: => Unit): Timer = {

    val timer = new DefaultTimer(operationTimeout)

    def doTask() = timer.executeIfNotCanceled {
      try { fun }
      finally { restart(interval) }
    }

    // only gets called once from calling thread, and then only from strand
    // The cancel function of the timer is guaranteed to be non-concurrent

    def restart(time: TimeInterval): Unit = {
      val t = exe.schedule(time)(this.enqueue(new Task(false)(doTask())))
      timer.onCancel(t.cancel())
    }

    restart(initial)
    timer
  }

  def terminate(fun: => Unit): Unit = {
    deferred.synchronized {
      if (!terminated) {
        terminated = true
        deferred.clear()
        val fut = this.future[Result[Unit]]
        def function() = {
          fun
          fut.set(Success())
        }
        exe.execute(enqueue(new Task(true)(function)))
        Some(fut)
      } else None
    }.foreach(_.await)
  }

  private val deferred = new collection.mutable.Queue[Task]()
  private var running = false
  private var terminated = false

  /**
   * Incoming request from an executor, to execute a task
   */
  private def checkQueue(): Unit = deferred.synchronized(acquire()).foreach(process)

  private def acquire(): Option[Task] = {
    if (running) None
    else {
      if (deferred.size > 0) {
        running = true
        Some(deferred.dequeue())
      } else None
    }
  }

  private def enqueue(task: Task): Boolean = deferred.synchronized {
    if (!terminated || task.isFinal) {
      deferred.enqueue(task)
      if (!running) exe.execute(checkQueue())
      true
    } else false
  }

  /**
   * Completion attempt from a task
   */
  private def release(): Unit = {
    deferred.synchronized {
      assert(running)
      running = false
      acquire()
    }.foreach { task =>
      exe.execute(process(task))
    }
  }

  private def process(task: Task): Unit = {
    try {
      task.perform()
    } catch {
      case ex: Exception => onException(ex)
    } finally {
      release()
    }
  }

  /**
   * delegate to parent executor if no handlers
   */
  override def onException(ex: Exception) = {
    if (handlers.isEmpty) {
      exe.onException(ex)
    } else {
      handlers.foreach(h => h.onException(ex))
    }
  }
}

