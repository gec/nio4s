package net.agileautomata.nio4s

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
import net.agileautomata.executor4s._

import java.nio.ByteBuffer

trait Channel {

  private val listeners = collection.mutable.Set.empty[Exception => Unit]

  def getExecutor: Executor

  // any exceptions that occur on the channel are passed to this handler, provides a nice way
  def listen(fun: Exception => Unit): Unit = listeners.synchronized(listeners.add(fun))

  // TODO - write
  protected def notifyListeners(ex: Exception) = listeners.synchronized(listeners.foreach(_.apply(ex)))

  def isOpen: Boolean

  def close(): Result[Unit]

  def read(buffer: ByteBuffer): Future[Result[ByteBuffer]]

  def write(buffer: ByteBuffer): Future[Result[Int]]

}