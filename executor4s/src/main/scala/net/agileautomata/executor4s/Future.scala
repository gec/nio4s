/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

trait Future[A] {
  def await(): A
  def listen(fun: A => Unit): Unit
}

trait Settable[A] {
  def set(value: A): Unit
}

object Result {

  def apply[A](fun: => A): Result[A] = {
    try { Success(fun) }
    catch { case ex: Exception => Failure(ex) }
  }

}

trait Result[+A] {
  // throws any exceptions on the calling thread
  def apply(): A
  def isSuccess: Boolean
  def isFailure: Boolean
}

case class Success[A](value: A) extends Result[A] {
  def apply() = value
  def isSuccess = true
  def isFailure = false
}

case class Failure(ex: Exception) extends Result[Nothing] {
  def apply() = throw ex
  def isSuccess = false
  def isFailure = true
}