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
import annotation.tailrec

trait Within {
  def within(timeoutms: Long): Unit
}

trait During {
  def during(timeoutms: Long): Unit
}

class SynchronizedVariable[A](default: A) {

  // TODO: this should be a writer/reader lock, awaitUntil functions can miss states because of "fair scheduling"
  // if many writers are queued to make an update one of them may win the "next access race" and the await
  // function wont see all of the currentValues in between the updates

  private var currentValue = default
  private val mutex = new Object

  def get = mutex.synchronized(currentValue)

  def set(newValue: A) = mutex.synchronized {
    currentValue = newValue
    mutex.notifyAll()
  }

  def modify(fun: A => A): A = mutex.synchronized {
    currentValue = fun(currentValue)
    mutex.notifyAll()
    currentValue
  }

  def awaitUntil(timeoutms: Long)(fun: A => Boolean): (A, Boolean) = mutex.synchronized {
    val expiration = System.currentTimeMillis + timeoutms

    @tailrec
    def await(): (A, Boolean) = {
      if (fun(currentValue)) {
        (currentValue, true)
      } else {
        val remaining = expiration - System.currentTimeMillis
        if (remaining <= 0) {
          (currentValue, false)
        } else {
          mutex.wait(remaining)
          await()
        }
      }
    }
    await()
  }

  def awaitWhile(timeoutms: Long)(fun: A => Boolean): (A, Boolean) = awaitUntil(timeoutms)(x => !fun(x))

  class Become(fun: A => Boolean)(evaluate: (Boolean, A, Long) => Unit) extends Within {
    def within(timeoutms: Long): Unit = {
      val (result, success) = awaitUntil(timeoutms)(fun)
      evaluate(success, result, timeoutms)
    }
  }

  class Remain(fun: A => Boolean)(evaluate: (Boolean, A, Long) => Unit) extends During {
    def during(timeoutms: Long): Unit = {
      val (result, success) = awaitWhile(timeoutms)(fun)
      evaluate(success, result, timeoutms)
    }
  }

  def shouldRemain(value: A): During = {
    def evaluate(failure: Boolean, last: A, timeout: Long) =
      if (failure) throw new Exception("Expected value to remain " + value + " for " + timeout + " ms but final value was " + last)
    new Remain(_ == value)(evaluate)
  }

  def shouldBecome(value: A): Within = {
    def evaluate(success: Boolean, last: A, timeout: Long) =
      if (!success) throw new Exception("Expected " + value + " within " + timeout + " ms but final value was " + last)
    new Become(_ == value)(evaluate)
  }
}