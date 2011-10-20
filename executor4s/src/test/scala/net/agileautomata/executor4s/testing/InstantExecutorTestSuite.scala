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
class InstantExecutorTestSuite extends FunSuite with ShouldMatchers {

  test("Instant executor ignores delay") {
    val exe = new InstantExecutor
    var i = 0
    val timer = exe.delay(10.days)(i = 1)
    i should equal(0)
    timer.cancel()
  }

  test("Execute is instantaneous") {
    val exe = new InstantExecutor
    var i = 0
    exe.execute(i = 1)
    i should equal(1)
  }

  test("Attempt is instantaneous") {
    val exe = new InstantExecutor
    var i = 0
    val future = exe.attempt(3 * 3)
    future.listen(r => i = r.get)
    i should equal(9)
    future.await should equal(Success(9))
  }

}