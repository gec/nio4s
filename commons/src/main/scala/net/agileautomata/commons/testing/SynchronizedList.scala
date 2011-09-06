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
package net.agileautomata.commons.testing

import annotation.tailrec

class SynchronizedList[A] {
  private var list: List[A] = Nil

  def append(a: Seq[A]) = synchronized {
    a.foreach(list ::= _)
    notifyAll()
  }

  def append(a: A) = synchronized {
    list ::= a
    notifyAll()
  }

  def shouldEqual(value: A*): BoundList = shouldEqual(value.toList)

  def shouldEqual(value: List[A]): BoundList = new BoundList(_ == value)((success, last, timeout) =>
    if (!success) throw new Exception("Expected " + value + " within " + timeout + " ms but final value was " + last))

  class BoundList(fun: List[A] => Boolean)(evaluate: (Boolean, List[A], Long) => Unit) {
    def within(timeoutms: Long) = {
      val (success, value) = waitForCondition(timeoutms)(fun)
      evaluate(success, value, timeoutms)
    }
  }

  def waitForCondition(timeoutms: Long)(condition: List[A] => Boolean): (Boolean, List[A]) = synchronized {
    val expiration = System.currentTimeMillis + timeoutms
    @tailrec
    def loop(): (Boolean, List[A]) = {
      if (condition(list)) (true, list)
      else {
        val remain = expiration - System.currentTimeMillis
        if (remain > 0) {
          wait(remain)
          loop()
        } else (false, list)
      }
    }
    loop()
  }

}

