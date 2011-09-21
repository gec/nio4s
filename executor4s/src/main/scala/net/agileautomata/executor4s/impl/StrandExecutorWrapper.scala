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

class StrandExecutorWrapper(exe: Executor) extends Strand with Callable {

  def execute(fun: => Unit) = exe.execute(post(Task(() => fun, false)))

  def delay(interval: TimeInterval)(fun: => Unit): Cancelable = {
    val task = Task(() => fun, false)
    val cancelable = exe.delay(interval)(post(task))
    new Cancelable {
      def cancel() = {
        task.isCanceled = true
        cancelable.cancel()
      }
    }
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
        exe.execute(post(Task(function, true)))
        Some(fut)
      } else None
    }.foreach(_.await)
  }

  def terminate() = terminate {}

  private case class Task(fun: () => Unit, isFinal: Boolean, var isCanceled: Boolean = false)

  private val deferred = new collection.mutable.Queue[Task]()
  private var running = false
  private var terminated = false

  /**
   * Incoming request from an executor, to execute a task
   */
  private def post(task: Task): Unit = {
    deferred.synchronized {
      if (!terminated || task.isFinal) {
        deferred.enqueue(task)
        acquire()
      } else None
    }.foreach(process)
  }

  private def acquire(): Option[Task] = {
    if (running) None
    else {
      if (deferred.size > 0) {
        running = true
        Some(deferred.dequeue())
      } else None
    }
  }

  /**
   * Completion call from a task
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
      if (!task.isCanceled) task.fun()
    } finally {
      release()
    }
  }

}

