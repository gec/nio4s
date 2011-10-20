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

private[impl] final class DefaultFuture[A](dispatcher: Executor, private var value: Option[A] = None) extends Future[A] with Settable[A] {

  private val mutex = new Object
  private val listeners = collection.mutable.Queue.empty[A => Unit]

  def isComplete = value.isDefined

  def replicate[B] = new DefaultFuture[B](dispatcher)
  def replicate[B](b: B) = new DefaultFuture[B](dispatcher, Some(b))

  def await(): A = mutex.synchronized {
    def get(): A = value match {
      case Some(x) => x
      case None =>
        mutex.wait()
        get()
    }
    get()
  }

  def listen(fun: A => Unit): Unit = mutex.synchronized {
    value match {
      case Some(x) => dispatcher.execute(fun(x))
      case None => listeners.enqueue(fun)
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
      listeners.foreach(l => dispatcher.execute(l.apply(result)))
    }
  }

}