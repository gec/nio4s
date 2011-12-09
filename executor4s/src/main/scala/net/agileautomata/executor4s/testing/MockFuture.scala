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
package net.agileautomata.executor4s.testing

import net.agileautomata.executor4s.SettableFuture

object MockFuture {
  def defined[A](a: A): MockFuture[A] = new MockFuture[A](Some(a))
  def undefined[A]: MockFuture[A] = new MockFuture[A](None)
}

final case class MockFuture[A](var value: Option[A]) extends SettableFuture[A] {

  private val listeners = collection.mutable.Queue.empty[A => Unit]
  private val onSetListeners = collection.mutable.Queue.empty[A => Unit]

  def replicate[B] = new MockFuture[B](None)
  def replicate[B](b: B) = new MockFuture[B](Some(b))

  def await: A = value match {
    case Some(x) => x
    case None => throw new Exception("Value is not set, blocking calls not allowed with MockFuture")
  }

  def listen(fun: A => Unit): Unit = value match {
    case Some(x) => fun(x)
    case None => listeners.enqueue(fun)
  }

  def onSet(fun: A => Unit): Unit = value match {
    case Some(x) => fun(x)
    case None => onSetListeners.enqueue(fun)
  }

  def isComplete = value.isDefined

  def set(a: A) = value match {
    case Some(x) => throw new Exception("Value is already set to: " + x)
    case None =>
      value = Some(a)
      onSetListeners.foreach(_.apply(a))
      listeners.foreach(_.apply(a))
  }
}