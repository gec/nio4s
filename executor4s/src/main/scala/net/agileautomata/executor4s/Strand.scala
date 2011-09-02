/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

/*
 *  A strand is an executor that guarantees operations
 *  are not executed concurrently. It makes no guarantees
 *  about ordering of submitted tasks.
 */
trait Strand extends Executor {

  // terminate the Strand, executing one last function
  // when terminate returns, the execution is complete
  // and no more tasks will run on this strand
  def terminate(fun: => Unit): Unit

  // terminate the strand, when terminate
  def terminate(): Unit
}

object Strand {

  def apply(exe: Executor): Strand = exe match {
    case s: Strand => s // don't re-wrap strands
    case e: Executor => new impl.StrandExecutorWrapper(exe)
  }
}