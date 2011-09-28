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
import java.util.concurrent.{ ScheduledExecutorService }
import com.weiglewilczek.slf4s.{ Logger, Logging }

private class FunRun(fun: => Unit, handler: ExceptionHandler.Callback) extends Runnable {
  def run() = {
    try {
      fun
    } catch {
      case ex: Exception => handler(ex)
    }
  }
}

private final class DecoratedExecutor(exe: ScheduledExecutorService, handler: ExceptionHandler.Callback)
    extends Callable with ExecutorService with Logging {

  override def execute(fun: => Unit): Unit = exe.execute(new FunRun(fun, handler))

  override def shutdown() = exe.shutdown()

  override def terminate(interval: TimeInterval) = {
    shutdown()
    exe.awaitTermination(interval.count, interval.timeunit)
  }

  override def delay(interval: TimeInterval)(fun: => Unit): Cancelable = {
    val future = exe.schedule(new FunRun(fun, handler), interval.count, interval.timeunit)
    new Cancelable {
      def cancel() { future.cancel(false) }
    }
  }
}

