/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s.api

import net.agileautomata.executor4s._

class ResultExecutor(exe: Executor) {

  def set[A](result: Settable[Result[A]])(fun: => Unit) = exe.execute(safely(result)(fun))

}