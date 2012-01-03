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

import net.agileautomata.executor4s.{ CancelTimeoutException, TimeInterval, Timer }

private final class DefaultTimer(cancelTimeout: TimeInterval) extends Timer {
  private var canceled = false
  private var executing = false
  private var terminate: Option[() => Unit] = None
  private val mutex = new Object

  def onCancel(fun: => Unit): Unit = terminate = Some(() => fun)

  def cancel() = mutex.synchronized {

    val start = System.nanoTime()

    def acquire(): Unit = {
      if (executing) {
        val elapsed = System.nanoTime() - start
        val remaining = (cancelTimeout.nanosec - elapsed) / 1000000 //convert from nanosec to millisec
        if (remaining > 0) {
          mutex.wait(remaining)
          acquire()
        } else throw new CancelTimeoutException(cancelTimeout)
      }
    }

    if (!canceled) {
      acquire()
      terminate.foreach(_.apply())
      terminate = None
      canceled = true
    }
  }

  def executeIfNotCanceled(fun: => Unit) = {
    def lock(): Boolean = mutex.synchronized {
      if (!canceled) {
        executing = true
        true
      } else false
    }
    def unlock(): Unit = mutex.synchronized {
      executing = false
      mutex.notifyAll()
    }
    if (lock()) {
      try { fun }
      finally { unlock() }
    }
  }

}