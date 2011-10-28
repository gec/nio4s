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
   * Uses the deafult ScheduledThreadPool implementation for both the executor and the scheduler
   */
  def newScheduledThreadPool(num: Int): ExecutorService = {
    val exe = JavaExecutors.newScheduledThreadPool(num)
    Defaults.executor(exe, exe)
  }

  /**
   * Overload that use the availableProcessors to size the pool
   */
  def newScheduledThreadPool(): ExecutorService = {
    val exe = JavaExecutors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors)
    Defaults.executor(exe, exe)
  }

  /**
   * A single scheduled thread
   */
  def newScheduledSingleThread(): ExecutorService = {
    val exe = JavaExecutors.newSingleThreadScheduledExecutor()
    Defaults.executor(exe, exe)
  }

  /**
   * Fully customizable. Uses separately specifiable executors for the executor and the schedule calls. This is mainly to overcome the limitation of
   * ScheduledExecutorService not being dynamically re-sizable.
   */
  def newCustomExecutor(exe: JExecutorService, scheduler: ScheduledExecutorService): ExecutorService =
    Defaults.executor(exe, scheduler)

  /**
   * Overload that uses the specified Schedule executor for both the execute and scheduled calls.
   */
  def newCustomExecutor(scheduler: ScheduledExecutorService): ExecutorService = newCustomExecutor(scheduler, scheduler)

}
