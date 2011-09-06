/**
 * Copyright 2011 J Adam Crain (jadamcrain@gmail.com)
 *
 * Licensed to J Adam Crain under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. J Adam Crain licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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