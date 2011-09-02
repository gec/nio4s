/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
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
    Range(0, 100).foreach(x => exe.execute(i = increment(i)))
    exe.terminate()
    i should be < 100
  }

  test("Strands do not execute concurrently") {
    val i = new SynchronizedVariable(0)

    val exe = Executors.newScheduledThreadPool()
    val strand = Strand(exe)
    Range(0, 100).foreach(x => strand.execute(i.set(increment(i.get))))
    i shouldEqual (100) within (5000)
    exe.terminate()
  }

  test("Strands can be terminated with a final task") {
    val i = new SynchronizedVariable(0)
    val exe = Executors.newScheduledThreadPool()
    val strand = Strand(exe)
    Range(0, 1000).foreach(x => strand.execute(i.set(increment(i.get)))) // fire off a bunch of tasks that will all try to increment
    strand.terminate(i.set(42))
    i.get() should equal(42)
    exe.terminate()
  }
}