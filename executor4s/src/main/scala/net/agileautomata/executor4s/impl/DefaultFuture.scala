/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.impl

import java.lang.IllegalStateException

import net.agileautomata.executor4s._

final class DefaultFuture[A](dispatcher: Executor) extends Future[A] with Settable[A] {

  private var value: Option[A] = None
  private val mutex = new Object
  private val listeners = collection.mutable.Queue.empty[A => Unit]

  private def notifyListeners(value: A) = mutex.synchronized {
    listeners.foreach(l => l.apply(value))
  }

  def await(): A = mutex.synchronized {
    def get(): A = value match {
      case Some(x) => x
      case None =>
        mutex.wait()
        get()
    }
    get()
  }

  def listen(fun: A => Unit): Unit = mutex.synchronized {
    value match {
      case Some(x) => dispatcher.execute(fun(x))
      case None => listeners.enqueue(fun)
    }
  }

  def isDone() = value.isDefined

  def set(result: A) = dispatcher.execute {
    mutex.synchronized {
      value match {
        case Some(x) => throw new IllegalStateException("Future has already been set to: " + value)
        case None =>
          value = Some(result)
          mutex.notifyAll()
      }
    }
    notifyListeners(result)
  }

}