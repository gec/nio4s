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

object Result {

  def apply[A](fun: => A): Result[A] = {
    try { Success(fun) }
    catch { case ex: Exception => Failure(ex) }
  }

}

sealed trait Result[+A] {
  // throws any exceptions on the calling thread
  def get: A
  def isSuccess: Boolean
  def isFailure: Boolean
  def map[B](convert: A => B): Result[B]
  def flatMap[B](convert: A => Result[B]): Result[B]
}

final case class Success[A](value: A) extends Result[A] {
  def get = value
  def isSuccess = true
  def isFailure = false
  def map[B](convert: A => B) = Success(convert(value))
  def flatMap[B](convert: A => Result[B]): Result[B]  = convert(value)
}

object Failure {
  def apply(msg: String): Failure = Failure(new Exception(msg))
}

final case class Failure(ex: Exception) extends Result[Nothing] {
  def get = throw ex
  def isSuccess = false
  def isFailure = true
  def map[B](convert: Nothing => B): Result[B] = this
  def flatMap[B](convert: Nothing => Result[B]) = this
}