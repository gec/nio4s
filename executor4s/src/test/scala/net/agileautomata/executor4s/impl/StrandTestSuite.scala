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
class StrandTestSuite extends FunSuite with ShouldMatchers {

  val defaultTimeout = 20000

  def increment(i: Int) = {
    Thread.sleep(1)
    i + 1
  }

  def fixture(testFun: Executor => Unit) = {
    val exe = Executors.newScheduledThreadPool()
    try { testFun(exe) }
    finally { exe.terminate() }
  }

  test("Standard thread pool executes concurrently if machine is multicore") {
    var i = 0
    fixture { exe => 1000.times(exe.execute(i = increment(i))) }
    i should be < 1000
  }

  test("Strands can have defined exception handlers") {
    fixture { exe =>
      val ex = new SynchronizedVariable[Option[String]](None)
      val strand = Strand(exe)
      val handler = new ExceptionHandler { def onException(e: Exception) = ex.set(Some(e.getMessage)) }
      strand.addExceptionHandler(handler)
      strand.execute(throw new Exception("test"))
      ex.shouldBecome(Some("test")) within (defaultTimeout)
    }
  }

  test("Strands do not execute concurrently") {
    fixture { exe =>
      val i = new SynchronizedVariable(0)
      val strand = Strand(exe)
      100.times(strand.execute(i.set(increment(i.get))))
      i shouldBecome (100) within (defaultTimeout)
    }
  }

  test("Strand task execute in sequence if executed from a single thread") {
    fixture { exe =>
      val list = new SynchronizedList[Int]
      val range = 1 to 100
      val strand = Strand(exe)
      range.foreach(i => strand.execute(list.append(i)))
      val expected = range.toList
      list shouldBecome expected within (defaultTimeout)
    }
  }

  test("Strands can be terminated with a final task") {
    val i = new SynchronizedVariable(0)
    fixture { exe =>
      val strand = Strand(exe)
      def execute: Unit = strand.execute { // reposts itself to the strand
        i.set(increment(i.get))
        execute
      }
      100.times(execute)
      strand.terminate(i.set(42))
      i.get should equal(42)
    }
    i.get should equal(42)
  }

  test("Final termination blocks until finished") {
    val i = new SynchronizedVariable(0)
    fixture { exe =>
      val strand = Strand(exe)
      strand.execute {
        Thread.sleep(1000)
        i.set(33)
      }
      strand.terminate(i.set(42))
      i.get should equal(42)
    }
    i.get should equal(42)
  }

  test("Strand tasks canceled on strand do not execute") {
    val i = new SynchronizedVariable(0)

    fixture { exe =>
      val strand = Strand(exe)
      strand.execute {
        1000.create(strand.schedule(0.seconds)(i.set(i.get + 1))).foreach(_.cancel())
      }
      strand.terminate()
    }

    i.get should equal(0)
  }

  test("Strand delayed tasks execute non-concurrently") {
    fixture { exe =>
      val i = new SynchronizedVariable(0)
      val strand = Strand(exe)
      val count = 100
      def newTimer = strand.schedule(1.milliseconds)(i.set(increment(i.get)))
      count.times(newTimer)
      i shouldBecome (count) within (defaultTimeout)
    }
  }

  test("Repeated timer works") {
    fixture { exe =>
      val i = new SynchronizedVariable[Int](0)
      val strand = Strand(exe)
      val timer = strand.scheduleWithFixedOffset(1.milliseconds)(i.modify(_ + 1))
      val (num, result) = i.awaitUntil(defaultTimeout)(_ > 10)
      timer.cancel()
      num should be > (10)
      result should equal(true)
    }
  }

  test("Repeated timer can be canceled") {
    fixture { exe =>
      val i = new SynchronizedVariable[Int](0)
      val strands = 1000.create(Strand(exe))
      val timers = strands.map(_.scheduleWithFixedOffset(0.milliseconds)(i.modify(_ + 1)))
      timers.foreach(_.cancel())
      val last = i.get
      i shouldRemain last during 500

    }
  }

  test("Attempt works from a strand") {
    fixture { exe =>
      val strand = Strand(exe)
      val result = exe.attempt {
        strand.attempt(3*3).await
      }.await
      result should equal(Success(Success(9)))
    }
  }

}