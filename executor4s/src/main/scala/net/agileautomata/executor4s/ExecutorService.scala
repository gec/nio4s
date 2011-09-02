/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

trait ExecutorService extends Executor {

  /**
   * Starts shutdown, but doesn't block until complete. Idempotent.
   */
  def shutdown(): Unit

  /**
   * Calls shutdown and blocks until the time interval expires
   * @return True if the executor terminates before the interval expires
   */
  def terminate(interval: TimeInterval): Boolean

  /**
   * Blocks indefinitely for the service to terminate
   */
  def terminate(): Unit = terminate(Long.MaxValue.days) // so long this is practically infinity
}