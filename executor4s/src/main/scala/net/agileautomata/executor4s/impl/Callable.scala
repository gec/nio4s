/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.impl

import net.agileautomata.executor4s._

trait Callable extends Executor {
  def call[A](fun: => A): Future[Result[A]] = {
    val f = new DefaultFuture[Result[A]](this)
    execute(f.set(Result(fun)))
    f
  }
}