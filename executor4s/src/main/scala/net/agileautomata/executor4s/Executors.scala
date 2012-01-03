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

import java.util.concurrent.{ Executors => JavaExecutors, ScheduledExecutorService, ExecutorService => JExecutorService }

import impl.Defaults

/*
* Factory object for creating all of the different executor service types
*/
object Executors {

  /**
   * Uses the deafult ScheduledThreadPool implementation for both the executor and the scheduler, only use
   * if application is 100% asynchronous, no await calls
   */
  def newScheduledThreadPool(num: Int, operationTimeout: TimeInterval): ExecutorService = {
    val exe = JavaExecutors.newScheduledThreadPool(num)
    Defaults.executor(exe, exe, operationTimeout)
  }

  /**
   * Overload that use the availableProcessors to size the pool, most performant option if application
   * is 100% asynchronous, no await calls
   */
  def newScheduledThreadPool(operationTimeout: TimeInterval): ExecutorService = {
    val exe = JavaExecutors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors)
    Defaults.executor(exe, exe, operationTimeout)
  }

  /**
   * Overload that use the availableProcessors to size the pool, most performant option if application
   * is 100% asynchronous, no await calls
   */
  def newScheduledThreadPool(): ExecutorService = {
    val exe = JavaExecutors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors)
    Defaults.executor(exe, exe, TimeInterval.EndOfTheUniverse)
  }

  /**
   * A single scheduled thread, only use if application is 100% asynchronous, no await calls
   */
  def newScheduledSingleThread(operationTimeout: TimeInterval = TimeInterval.EndOfTheUniverse): ExecutorService = {
    val exe = JavaExecutors.newSingleThreadScheduledExecutor()
    Defaults.executor(exe, exe, operationTimeout)
  }

  /**
   * Dynamic threadPool for execution of work (dynamically expands threads to handle spike in workload)
   * and a single thread for scheduling delayed or repeated work. Use if there are any awaits() in application.
   *
   * Implemented using newCachedThreadPool and newSingleThreadScheduledExecutor.
   */
  def newResizingThreadPool(operationTimeout: TimeInterval = TimeInterval.EndOfTheUniverse): ExecutorService = {
    val scheduler = JavaExecutors.newSingleThreadScheduledExecutor()
    val executor = JavaExecutors.newCachedThreadPool()
    Defaults.executor(executor, scheduler, operationTimeout)
  }

  /**
   * Fully customizable. Uses separately specifiable executors for the executor and the schedule calls. This is mainly to overcome the limitation of
   * ScheduledExecutorService not being dynamically re-sizable.
   */
  def newCustomExecutor(exe: JExecutorService, scheduler: ScheduledExecutorService,
    operationTimeout: TimeInterval): ExecutorService =
    Defaults.executor(exe, scheduler, operationTimeout)

  /**
   * Overload that uses the specified Schedule executor for both the execute and scheduled calls.
   */
  def newCustomExecutor(scheduler: ScheduledExecutorService,
    operationTimeout: TimeInterval): ExecutorService =
    newCustomExecutor(scheduler, scheduler, operationTimeout)

}
