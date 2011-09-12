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
package net.agileautomata.nio4s.channels.tcp

import java.nio.ByteBuffer

import net.agileautomata.nio4s.api._
import java.nio.channels.{ Selector, SocketChannel => NioSocketChannel }
import net.agileautomata.executor4s._
import impl.DefaultFuture

final class TcpChannel(channel: NioSocketChannel, selector: Selector, multiplexer: Executor, dispatcher: Executor) extends Channel {

  def isOpen = channel.isOpen()

  def getExecutor = dispatcher

  def close(): Result[Unit] = {
    val promise = new DefaultFuture[Result[Unit]](dispatcher)
    multiplexer.set(promise) {
      channel.close()
      promise.set(Success())
    }
    selector.wakeup()
    promise.await()
  }

  def read(buffer: ByteBuffer): DefaultFuture[Result[ByteBuffer]] = {
    val promise = new DefaultFuture[Result[ByteBuffer]](dispatcher)
    multiplexer.set(promise) {
      val a = Attachment(channel, selector).registerRead(finishRead(buffer, promise))
      channel.register(selector, a.interestOps, a)
    }
    selector.wakeup()
    promise
  }

  def write(buffer: ByteBuffer): Future[Result[Int]] = {
    val promise = new DefaultFuture[Result[Int]](multiplexer)
    multiplexer.set(promise) {
      val a = Attachment(channel, selector).registerWrite(finishWrite(buffer, promise))
      channel.register(selector, a.interestOps, a)
    }
    selector.wakeup()
    promise
  }

  private def finishWrite(buffer: ByteBuffer, settable: Settable[Result[Int]]): Option[Registration] = {
    try {
      val num = channel.write(buffer)
      settable.set(Success(num))
    } catch {
      case ex: Exception =>
        dispatcher.execute(notifyListeners(ex))
        settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

  private def finishRead(buffer: ByteBuffer, settable: Settable[Result[ByteBuffer]]): Option[Registration] = {
    try {
      val num = channel.read(buffer)
      if (num < 0) throw new Exception("End of stream (read -1) reached while reading")
      else settable.set(Success(buffer))
    } catch {
      case ex: Exception =>
        dispatcher.execute(notifyListeners(ex))
        settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

}