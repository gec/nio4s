/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

/**
 * An executor is an interface that provides a way to execute tasks asynchronously. No guarantees
 * are made about what threads the submitted tasks are run on and they can run concurrently.
 */

trait Executor {

  def execute(fun: => Unit): Unit

  def call[A](fun: => A): Future[Result[A]]

  def delay(interval: TimeInterval)(fun: => Unit): Cancelable

}

