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
import testing._

@RunWith(classOf[JUnitRunner])
class StrandTest extends FunSuite with ShouldMatchers {

  def increment(i: Int) = {
    Thread.sleep(1)
    i + 1
  }

  test("Standard thread pool executes concurrently") {
    var i = 0
    val exe = Executors.newScheduledThreadPool()
    100.times(exe.execute(i = increment(i)))
    exe.terminate()
    i should be < 100
  }

  test("Strands do not execute concurrently") {
    val i = new SynchronizedVariable(0)
    val exe = Executors.newScheduledThreadPool()
    val strand = Strand(exe)
    100.times(strand.execute(i.set(increment(i.get))))
    i shouldEqual (100) within (5000)
    exe.terminate()
  }

  test("Strands can be terminated with a final task") {
    val i = new SynchronizedVariable(0)
    val exe = Executors.newScheduledThreadPool()
    val strand = Strand(exe)
    1000.times(strand.execute(i.set(increment(i.get)))) // fire off a bunch of tasks that will all try to increment
    strand.terminate(i.set(42))
    i.get() should equal(42)
    exe.terminate()
  }

  test("Strand tasks canceled on strand do not execute") {
    val i = new SynchronizedVariable(0)
    val exe = Executors.newScheduledThreadPool()
    val strand = Strand(exe)

    strand.execute {
      1000.create(strand.delay(0.seconds)(i.set(i.get + 1))).foreach(_.cancel())
    }

    strand.terminate()
    exe.terminate()
    i.get() should  equal(0)
  }
}