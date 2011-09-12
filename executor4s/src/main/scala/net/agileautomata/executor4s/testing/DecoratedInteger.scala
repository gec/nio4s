package net.agileautomata.executor4s.testing

import annotation.tailrec

class DecoratedInteger(i: Int) extends Traversable[Int] {
  assert(i >= 0)

  def foreach[A](f: Int => A): Unit = {
    @tailrec
    def loop(value: Int): Unit = if(value <= i) {
      f(value)
      loop(value+1)
    }
    loop(1)
  }

  def times(f: => Unit): Unit = foreach(_ => f)

  def create[A](f: => A): Traversable[A] = map(x => f)
}