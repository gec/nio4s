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
import annotation.tailrec
import net.agileautomata.executor4s._
import net.agileautomata.commons.testing._

@RunWith(classOf[JUnitRunner])
class FuturesTestSuite extends FunSuite with ShouldMatchers {

  val fib100str = "354224848179261915075"
  val fib100 = BigInt(fib100str)
  val digits = 21

  def fixture(test: Executor => Unit): Unit = {
    val exe = Executors.newScheduledThreadPool()
    try { test(exe) } finally { exe.terminate() }
  }

  def fib(i: Int): BigInt = {
    assert(i >= 0)
    @tailrec
    def next(j: Int, last: BigInt, current: BigInt): BigInt = {
      if (j == i) last
      else next(j + 1, current, current + last)
    }
    next(0, 0, 1)
  }

  test("Futures can be created via a attempt") {
    fixture { exe =>
      val x = exe.attempt(fib(100))
      x.await should equal(Success(fib100))
    }
  }

  test("Futures can be mapped") {
    fixture { exe =>
      exe.attempt(fib(100)).map(_.get.toString).await should equal(fib100str)
    }
  }

  test("Futures can be flatmap-ed") {
    fixture { exe =>
      val future = exe.attempt(fib(100))
      def eval(r: Result[BigInt]): Future[Result[String]] = r match {
        case Success(bi) => exe.attempt(bi.toString)
        case f: Failure => future.replicate(f)
      }
      future.flatMap(eval).await should equal(Success(fib100str))
    }
  }

  test("Futures can be combined in for-comprehension") {
    fixture { exe =>
      val f1 = exe.attempt(fib(100))
      val f2 = exe.attempt(fib(100))

      val f3 = for {
        r1 <- f1
        r2 <- f2
      } yield (r1.get + r2.get)

      f3.await should equal(BigInt(2) * fib100)
    }
  }

  test("Futures of the same type can be gathered") {
    fixture { exe =>
      val f1 = exe.attempt(fib(100))
      val f2 = exe.attempt(fib(100))
      val seq = Futures.gather(exe, List(f1, f2))
      seq.await.toList should equal(List(Success(fib100), Success(fib100)))
    }
  }

  test("Futures can be gathered and mapped") {
    fixture { exe =>
      val f1 = exe.attempt(fib(100))
      val f2 = exe.attempt(fib(100))
      val seq = Futures.gatherMap(exe, List(f1, f2))(_.get)
      seq.await.toList should equal(List(fib100, fib100))
    }
  }

  test("Gather handles an  empty list") {
    fixture { exe =>
      Futures.gather(exe, Nil: List[Future[Int]]).await should equal(Nil)
    }
  }

}