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

import java.lang.IllegalStateException

import net.agileautomata.executor4s._

private final class DefaultFuture[A](dispatcher: Executor, private var value: Option[A] = None) extends SettableFuture[A] {

  private val mutex = new Object
  private val listeners = collection.mutable.Set.empty[A => Unit]
  private val onSetListeners = collection.mutable.Set.empty[A => Unit]

  def isComplete = value.isDefined

  def replicate[B] = new DefaultFuture[B](dispatcher)
  def replicate[B](b: B) = new DefaultFuture[B](dispatcher, Some(b))

  def await(): A = mutex.synchronized {
    val start = System.nanoTime()
    def get(): A = value match {
      case Some(x) => x
      case None =>
        val elapsed = System.nanoTime() - start
        val remainderMs = (dispatcher.operationTimeout.nanosec - elapsed) / 1000000
        if (remainderMs > 0) {
          mutex.wait(remainderMs)
          get()
        } else throw new AwaitTimeoutException(dispatcher.operationTimeout)
    }
    get()
  }

  def listen(fun: A => Unit): Unit = mutex.synchronized {
    listeners.add(fun)
    value.foreach(result => callbackListener(result)(fun))
  }

  private def callbackListener(result: A)(fun: A => Unit): Unit = {
    dispatcher.execute {
      try { fun.apply(result) }
      finally {
        mutex.synchronized {
          listeners.remove(fun)
          mutex.notifyAll()
        }
      }
    }
  }

  def set(result: A) = {
    mutex.synchronized {
      value match {
        case Some(x) => throw new IllegalStateException("Future has already been set to: " + value)
        case None =>
          value = Some(result)
          mutex.notifyAll()
      }
      onSetListeners.foreach(fun => callbackOnSet(result)(fun))
      listeners.foreach(fun => callbackListener(result)(fun))
    }
  }

  def onSet(fun: A => Unit): Unit = mutex.synchronized {
    onSetListeners.add(fun)
    value.foreach(result => callbackOnSet(result)(fun))
  }

  private def callbackOnSet(result: A)(fun: A => Unit): Unit = {
    try { fun.apply(result) }
    finally {
      mutex.synchronized {
        onSetListeners.remove(fun)
        mutex.notifyAll()
      }
    }
  }
}