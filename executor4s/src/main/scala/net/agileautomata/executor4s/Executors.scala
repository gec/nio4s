/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

import java.util.concurrent.{ Executors => JavaExecutors }

import impl._

/*
* Factory object for creating all of the different executor service types
*/
object Executors {
  def newScheduledThreadPool(num: Int = Runtime.getRuntime.availableProcessors()): ExecutorService = new DecoratedExecutor(JavaExecutors.newScheduledThreadPool(num))
  def newScheduledSingleThread(): ExecutorService = new DecoratedExecutor(JavaExecutors.newSingleThreadScheduledExecutor())
}