/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.testing

import annotation.tailrec

class SynchronizedVariable[A](defaultValue: A) {

  private var currentValue = defaultValue
  private val mutex = new Object

  def get() = mutex.synchronized(currentValue)

  def set(newValue: A) = mutex.synchronized {
    currentValue = newValue
    mutex.notifyAll()
  }

  def await(timeoutms: Long)(fun: A => Boolean): (A, Boolean) = mutex.synchronized {
    val expiration = System.currentTimeMillis() + timeoutms

    @tailrec
    def await(): (A, Boolean) = {
      if (fun(currentValue)) (currentValue, true)
      else {
        val remaining = expiration - System.currentTimeMillis
        if (remaining <= 0) (currentValue, false)
        else {
          mutex.wait(remaining)
          await()
        }
      }
    }
    await()
  }

  class BoundValue(fun: A => Boolean)(evaluate: (Boolean, A, Long) => Unit) {
    def within(timeoutms: Long): Unit = {
      val (result, success) = await(timeoutms)(fun)
      evaluate(success, result, timeoutms)
    }
  }

  def shouldEqual(value: A): BoundValue = {
    def evaluate(success: Boolean, last: A, timeout: Long) =
      if (!success) throw new Exception("Expected " + value + " within " + timeout + " ms but final value was " + last)
    new BoundValue(_ == value)(evaluate)
  }
}