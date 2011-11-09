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

@RunWith(classOf[JUnitRunner])
class ExecutorTestSuite extends FunSuite with ShouldMatchers {

  def fixture(fun: ExecutorService => Unit): Unit = {
    val exe = Executors.newScheduledSingleThread()
    try { fun(exe) }
    finally { exe.terminate() }
  }

  test("Unhandled exceptions can be matched") {
    fixture { exe =>
      val f = exe.attempt(1 / 0)
      f.await.isFailure should equal(true)
      intercept[ArithmeticException](f.await.get)
    }
  }

  test("Results/Futures can be combined in for-comprehension") {
    fixture { exe =>
      val f1 = exe.attempt(3 * 3)
      val f2 = exe.attempt(4 * 4)

      val f3 = Results.combine(f1, f2)(_ + _)

      // doesn't block until  we await! combines
      f3.await.get should equal(5 * 5)
    }
  }

  test("If one input is a failure, the entire result is a failure") {
    fixture { exe =>
      val f1 = exe.attempt(1 / 0)
      val f2 = exe.attempt(42)

      val f3 = Results.combine(f1, f2)(_ * _)
      intercept[ArithmeticException](f3.await.get)
    }
  }

  test("Executor termination is idempotent") {
    fixture { exe =>
      exe.terminate()
    }
  }

  test("scheduleAtFixedOffset repeats") {
    fixture { exe =>
      val list = new SynchronizedList[Int]
      exe.scheduleWithFixedOffset(0.milliseconds, 10.milliseconds)(list.append(42))
      list shouldBecome (42, 42, 42) within 5000
    }
  }

  test("scheduleAtFixedOffset can be canceled synchronously") {
    fixture { exe =>
      val list = new SynchronizedList[Int]
      val futures = 1000.create(exe.scheduleWithFixedOffset(0.milliseconds, 10.milliseconds)(list.append(42)))
      futures.foreach(_.cancel())
      val finalValue = list.get
      list shouldRemain finalValue during 50
    }
  }

}