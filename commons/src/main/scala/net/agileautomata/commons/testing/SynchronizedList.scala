/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
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

