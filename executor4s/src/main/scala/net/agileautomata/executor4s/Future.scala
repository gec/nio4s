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

/**
 * Represents a computation whose value can be set
 */
trait Settable[A] {
  def set(value: A): Unit
}

/**
 * Represents the future result of an asynchronous computation
 */
trait Future[A] {

  /**
   * Block for the operation to complete. Will throw DefaultTimeoutException if the executor's default timeout elapses.
   * @return value of type A
   */
  def await: A

  /**
   * Asynchronously listen for the result. The listening function is called from the Future's dispatcher.
   * @param fun notification function that is notified when the operation completes
   */
  def listen(fun: A => Unit): Unit

  /**
   * @return True if the operation is complete, false otherwise
   */
  def isComplete: Boolean

  /**
   * @return new settable future, with this future's underlying dispatcher
   */
  def replicate[B]: SettableFuture[B]

  /**
   * @param b The predefined value of the operation
   * @return new defined, future with this future's underlying dispatcher.
   */
  def replicate[B](b: B): SettableFuture[B]

  /**
   *  Transforms the future into a future of another type
   *  @param f Transform function that maps a type A to a type B
   *  @return future of type B
   */
  def map[B](f: A => B): Future[B] = new WrappedFuture(this, f)

  /**
   * Transforms the future into a future of another type.
   *
   * Note: the flatMap block will be executed by the thread calling set on this future and
   * should not block at all.
   *
   * @param f Transform function that maps type A to a Future[B]
   * @reutrn future of type B
   */
  def flatMap[B](f: A => Future[B]): Future[B] = {
    val future = replicate[B]
    this.onSet(a => f(a).onSet(future.set))
    future
  }

  /**
   * a listen() replacement that is called by the original thread that called set, not the executor
   * thread
   */
  protected def onSet(fun: A => Unit): Unit

  private class WrappedFuture[A, B](f: Future[A], convert: A => B) extends Future[B] {
    def await: B = convert(f.await)
    def listen(fun: B => Unit): Unit = f.listen(a => fun(convert(a)))
    def onSet(fun: B => Unit): Unit = f.onSet(a => fun(convert(a)))
    def isComplete = f.isComplete
    def replicate[B] = f.replicate[B]
    def replicate[B](b: B) = f.replicate[B](b)
  }

}

/**
 * settable future, Future is the consumer facing interface and Settable is for the future implementers
 */
trait SettableFuture[A] extends Future[A] with Settable[A]

