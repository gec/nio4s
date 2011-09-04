/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s

import net.agileautomata.executor4s._
import java.net.InetSocketAddress

package object api {

  implicit def convertExecutorToResultExecutor(exe: Executor) = new ResultExecutor(exe)

  def localhost(port: Int) = new InetSocketAddress("127.0.0.1", port)

  def safely[A](result: Settable[Result[A]])(fun: => Unit) = {
    try {
      fun
    } catch {
      case ex: Exception => result.set(Failure(ex))
    }
  }
}
