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
package net.agileautomata.executor4s.impl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import net.agileautomata.executor4s._
import net.agileautomata.commons.testing._
import java.lang.IllegalStateException

@RunWith(classOf[JUnitRunner])
class ExecutorTestSuite extends FunSuite with ShouldMatchers {

  def fixture(fun: Executor => Unit): Unit = {
    val exe = Executors.newScheduledSingleThread()
    try { fun(exe) }
    finally { exe.shutdown() }
  }

  test("Unhandled exceptions can be matched") {
    def throwEx: Int = throw new IllegalStateException("foobar")

    fixture { exe =>
      val f = exe.call(throwEx)
      f.await.isFailure should equal(true)
      intercept[IllegalStateException](f.await.apply())
    }
  }

  // TODO - test can only be observed... any backends that can be hooked into?
  test("Unhandled exceptions are logged") {
    fixture { exe =>
      exe.execute(throw new Exception("This exception was intentionally thrown in a test"))
    }
  }

}