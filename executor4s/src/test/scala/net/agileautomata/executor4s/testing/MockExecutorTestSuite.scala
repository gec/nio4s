package net.agileautomata.executor4s.testing

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
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import net.agileautomata.executor4s._

@RunWith(classOf[JUnitRunner])
class MockExecutorTestSuite extends FunSuite with ShouldMatchers {

  test("No execution returns false") {
    var num = 0
    val exe = new MockExecutor
    exe.isIdle should equal(true)
    exe.runNextPendingAction() should equal(false)
  }

  test("Single execution works") {
    var num = 0
    val exe = new MockExecutor
    exe.execute(num += 1)
    exe.runNextPendingAction() should equal(true)
    num should equal(1)
  }

  test("Timer tick works") {
    var num = 0
    val exe = new MockExecutor
    exe.delay(2.seconds)(num += 1)
    exe.tick(1999.milliseconds)
    num should equal(0)
    exe.tick(1.milliseconds)
    num should equal(1)
  }

  test("Timers can be canceled") {
    var num = 0
    val exe = new MockExecutor
    val timer = exe.delay(2.seconds)(num += 1)
    timer.cancel()
    exe.tick(3.seconds)
    num should equal(0)
  }

  test("Executes timers in the correct order") {
    var list: List[Int] = Nil
    val exe = new MockExecutor
    exe.delay(1.seconds)(list ::= 2)
    exe.delay(2.seconds)(list ::= 1)
    exe.tick(1.days)
    list should equal(List(1, 2))
  }

  test("Blocking await throws exception to prevent deadlock") {
    val exe = new MockExecutor
    val f = exe.attempt(1 + 1)
    intercept[Exception](f.await)
  }

  test("Attempt() queues an action just like execute does") {
    val exe = new MockExecutor
    val f = exe.attempt(1 + 1)
    exe.runNextPendingAction() should equal(true)
    f.await should equal(Success(2))
  }

  test("Detects infinite recursion via execute") {
    val exe = new MockExecutor
    def recurse(): Unit = exe.execute(recurse())
    exe.execute(recurse())
    intercept[Exception](exe.runUntilIdle())
  }

  test("Detects infinite recursion via tick") {
    val exe = new MockExecutor
    def recurse(): Unit = exe.delay(0.seconds)(recurse())
    exe.delay(100.seconds)(recurse())
    intercept[Exception](exe.tick(100.seconds))
  }

}