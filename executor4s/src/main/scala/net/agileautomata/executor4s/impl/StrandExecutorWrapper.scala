/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.impl

import net.agileautomata.executor4s._

class StrandExecutorWrapper(exe: Executor) extends Strand with Callable {

  def execute(fun: => Unit) = exe.execute(post(Task(() => fun, false)))

  def delay(interval: TimeInterval)(fun: => Unit): Cancelable = {
    exe.delay(interval)(post(Task(() => fun, false)))
  }

  def terminate(fun: => Unit): Unit = {
    deferred.synchronized {
      if (!terminated) {
        terminated = true
        deferred.clear()
        Some(exe.call(post(Task(() => fun, true))))
      } else None
    }.foreach(_.await())
  }

  def terminate() = terminate {}

  private case class Task(fun: () => Unit, isFinal: Boolean)

  private val deferred = new collection.mutable.Queue[Task]()
  private var running = false
  private var terminated = false

  /**
   * Incoming request from an executor, to execute a task
   */
  private def post(task: Task): Unit = {
    deferred.synchronized {
      if (!terminated || task.isFinal) {
        deferred.enqueue(task)
        acquire()
      } else None
    }.foreach(task => process(task))
  }

  private def acquire(): Option[Task] = {
    if (running) None
    else {
      if (deferred.size > 0) {
        running = true
        Some(deferred.dequeue())
      } else None
    }
  }

  /**
   * Completion call from a task
   */
  private def release(): Unit = {
    deferred.synchronized {
      assert(running)
      running = false
      acquire()
    }.foreach { task =>
      exe.execute(process(task))
    }
  }

  private def process(task: Task): Unit = {
    try {
      task.fun()
    } finally {
      release()
    }
  }

}

