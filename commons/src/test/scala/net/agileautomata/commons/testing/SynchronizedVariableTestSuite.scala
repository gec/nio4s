package net.agileautomata.commons.testing

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

import java.lang.Thread

@RunWith(classOf[JUnitRunner])
class SynchronizedVariableTestSuite extends FunSuite with ShouldMatchers {

  test("Get/set is works as expected") {
    val num = new SynchronizedVariable(0)
    onAnotherThread(num.set(num.get + 1))
    num shouldBecome 1 within 5000
  }

  test("Modify is atomic") {
    val num = new SynchronizedVariable(0)

    100 times onAnotherThread(num.modify(_ + 1))

    num shouldBecome 100 within 5000
    num shouldRemain 100 during 500
  }

  test("Become test fails with exception") {
    val num = new SynchronizedVariable(0)
    intercept[Exception](num shouldBecome 1 within 100)
  }

  test("Remain test fails with exception") {
    val num = new SynchronizedVariable(0)
    onAnotherThread(num.modify(_ + 1))
    intercept[Exception](num shouldRemain 0 during 100)
  }

}