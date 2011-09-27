package net.agileautomata.executor4s

import com.weiglewilczek.slf4s.Logging

trait ExceptionHandler {
  def apply(ex: Exception): Unit
}

object LoggingExceptionHandler extends ExceptionHandler with Logging {

  def apply(ex: Exception) = logger.error("Unhandled exception in executor", ex)

}