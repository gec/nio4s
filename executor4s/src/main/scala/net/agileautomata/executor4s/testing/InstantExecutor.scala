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
package net.agileautomata.executor4s.testing

import net.agileautomata.executor4s._

object NullTimer extends Timer {
  def cancel() = {}
}

/**
 * A very simple executor implementation that instantly calls attempt/execute blocks. Delay calls are ignored.
 */
class InstantExecutor extends Executor {

  def attempt[A](fun: => A) = new MockFuture(Some(Success(fun)))

  def schedule(interval: TimeInterval)(fun: => Unit): Timer = NullTimer

  def scheduleWithFixedOffset(initial: TimeInterval, interval: TimeInterval)(fun: => Unit): Timer = NullTimer

  def execute(fun: => Unit) = fun

}