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

import java.lang.Object

/**
 * Provides a way to execute tasks asynchronously. No guarantees
 * are made about what threads the submitted tasks are run on
 * and they can run concurrently.
 */

trait Executor {

  /**
   * The amount of time that await/cancel calls will block until they throw an exception
   */
  def operationTimeout: TimeInterval

  /**
   * Execute a unit of work asynchronously. Fire and forget.
   */
  def execute(fun: => Unit): Unit

  /**
   * Execute a unit of work asynchronously. Use this method when the work function can throw an exception. Results provide clean pattern matching
   * semantics for handling Success/Failure:
   */
  def attempt[A](fun: => A): Future[Result[A]]

  /**
   * Execute a unit of work once, after the interval elapses
   */
  def schedule(interval: TimeInterval)(fun: => Unit): Timer

  /**
   * Execute a unit of work repeatably with a fixed offset between the completion of the last event and the beginning of the next
   */
  def scheduleWithFixedOffset(initial: TimeInterval, offset: TimeInterval)(fun: => Unit): Timer

  /**
   * Overload for scheduleWithFixedOffset that immediately fires the first timer event
   */
  final def scheduleWithFixedOffset(offset: TimeInterval)(fun: => Unit): Timer =
    scheduleWithFixedOffset(0.nanoseconds, offset)(fun)

  /**
   * Create a settable future that dispatches from this executor
   */
  final def future[A]: SettableFuture[A] = Future[A](this)

  /**
   * Add exception handler. Any exceptions are forwarded to this handler.
   */
  def addExceptionHandler(handler: ExceptionHandler): Unit = mutex.synchronized(handlers += handler)

  /**
   *  Remove an exception handler
   */
  def removeExceptionHandler(handler: ExceptionHandler): Unit = mutex.synchronized(handlers -= handler)

  protected def onException(ex: Exception) = handlers.foreach(h => execute(h.onException(ex)))

  private val mutex = new Object
  private var handlers = scala.collection.immutable.Set.empty[ExceptionHandler]

}

