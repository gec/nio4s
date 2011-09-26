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

import impl.Defaults

object Future {
  def apply[A](executor: Executor): Future[A] with Settable[A] = Defaults.future[A](executor)
}

trait Awaitable[A] {
  def await: A
}

trait Settable[A] {
  def set(value: A): Unit
}

trait Future[A] extends Awaitable[A] {

  def await: A
  def listen(fun: A => Unit): Unit
  def isComplete: Boolean

  /** creates a new future, with this future's underlying dispatcher */
  def replicate[B]: Future[B] with Settable[B]

  /**
   * creates  a new future, with this future's underlying dispatcher. The
   * value is already defined
   */
  def replicate[B](b: B): Future[B] with Settable[B]

  private class WrappedFuture[A, B](f: Future[A], convert: A => B) extends Future[B] {
    def await: B = convert(f.await)
    def listen(fun: B => Unit): Unit = f.listen(a => fun(convert(a)))
    def isComplete = f.isComplete
    def replicate[B] = f.replicate[B]
    def replicate[B](b: B) = f.replicate[B](b)
  }

  def map[B](f: A => B): Future[B] = new WrappedFuture(this, f)

  def flatMap[B](f: A => Future[B]): Future[B] = {
    val future = replicate[B]
    this.listen(a => f(a).listen(future.set))
    future
  }

}

