/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.commons.testing

import annotation.tailrec

class DecoratedInteger(num: Int) extends Traversable[Int] {

  assert(num >= 0)

  def times(fun: => Unit) = foreach(x => fun)

  def foreach[A](fun: Int => A): Unit = {
    @tailrec
    def count(i: Int): Unit = if (i <= num) {
      fun(i)
      count(i + 1)
    }
    count(1)
  }
}