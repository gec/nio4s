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

/**
 * implements the Executor interface by passing all calls through to real executor
 */
trait ExecutorDelegate extends Executor {
  /**
   * implementing classes need to define the executor to handle all of the calls
   */
  protected def executor: Executor

  final override def operationTimeout = executor.operationTimeout
  final override def execute(fun: => Unit): Unit = executor.execute(fun)
  final override def attempt[A](fun: => A): Future[Result[A]] = executor.attempt(fun)
  final override def schedule(interval: TimeInterval)(fun: => Unit): Timer = executor.schedule(interval)(fun)
  final override def scheduleWithFixedOffset(initial: TimeInterval, offset: TimeInterval)(fun: => Unit): Timer =
    executor.scheduleWithFixedOffset(initial, offset)(fun)
  final override def onException(ex: Exception) = executor.onException(ex)
  final override def future[A] = executor.future[A]
}