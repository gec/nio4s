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


import java.util.concurrent.{ ScheduledExecutorService => JScheduledExecutorService, ExecutorService => JExecutorService, TimeUnit }
import net.agileautomata.executor4s._
import net.agileautomata.log4s.Logging

private class FunRun(handler: Exception => Unit)(fun: => Unit) extends Runnable {
  def run() = {
    try {
      fun
    } catch {
      case ex: Exception => handler(ex)
    }
  }
}

private final class DecoratedExecutor(exe: JExecutorService, scheduler: JScheduledExecutorService, val operationTimeout: TimeInterval)
    extends Callable with ExecutorService with Logging {

  override def future[A] = new DefaultFuture[A](this)

  override def execute(fun: => Unit): Unit = exe.execute(new FunRun(onException)(fun))

  override def shutdown() = {
    scheduler.shutdown()
    exe.shutdown()
  }

  override def terminate(interval: TimeInterval) = {
    shutdown()
    scheduler.awaitTermination(interval.count, interval.timeunit)
    exe.awaitTermination(interval.count, interval.timeunit)
  }

  override def schedule(interval: TimeInterval)(fun: => Unit): Timer = {
    val timer = new DefaultTimer(operationTimeout)
    // do the actual dispatching of the work from the executor pool since it can resize
    val runnable = new FunRun(onException)(execute(timer.executeIfNotCanceled(fun)))
    val future = scheduler.schedule(runnable, interval.nanosec, TimeUnit.NANOSECONDS)
    timer.onCancel(future.cancel(false))
    timer
  }

  override def scheduleWithFixedOffset(initial: TimeInterval, offset: TimeInterval)(fun: => Unit): Timer = {
    val timer = new DefaultTimer(operationTimeout)

    def restart(interval: TimeInterval): Unit = {
      val runnable = new FunRun(onException)(function(offset))
      val future = scheduler.schedule(runnable, interval.nanosec, TimeUnit.NANOSECONDS)
      timer.onCancel(future.cancel(false))
    }

    def function(interval: TimeInterval): Unit = {
      execute { // the work gets done on the dispatcher
        timer.executeIfNotCanceled {
          try { fun }
          finally { restart(interval) }
        }
      }
    }

    restart(initial)
    timer
  }

  /**
   * standard decorated executor logs errors using slf4j if no specific exception handler
   * is included.
   */
  override def onException(ex: Exception) = {
    if (handlers.isEmpty) {
      LoggingExceptionHandler.onException(ex)
    } else {
      handlers.foreach(h => execute(h.onException(ex)))
    }
  }
}

