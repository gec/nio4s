package net.agileautomata.nio4s.impl.tcp

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
import java.nio.ByteBuffer

import net.agileautomata.nio4s._
import impl.{ Registration, Attachment }
import java.nio.channels.{ Selector, SocketChannel => NioSocketChannel }
import net.agileautomata.executor4s._

final class TcpChannel(channel: NioSocketChannel, selector: Selector, multiplexer: Executor, dispatcher: Executor) extends Channel {

  def isOpen = channel.isOpen()

  def getExecutor = dispatcher

  def close(): Result[Unit] = {
    val future = dispatcher.future[Result[Unit]]
    multiplexer.execute {
      set(future)(channel.close())
    }
    selector.wakeup()
    future.await
  }

  def read(buffer: ByteBuffer): Future[Result[ByteBuffer]] = {
    val future = dispatcher.future[Result[ByteBuffer]]
    multiplexer.execute {
      setOnException(future) {
        val a = Attachment(channel, selector).registerRead(finishRead(buffer, future))
        channel.register(selector, a.interestOps, a)
      }
    }
    selector.wakeup()
    future
  }

  def write(buffer: ByteBuffer): Future[Result[Int]] = {
    val future = dispatcher.future[Result[Int]]
    multiplexer.execute {
      setOnException(future) {
        val a = Attachment(channel, selector).registerWrite(finishWrite(buffer, future))
        channel.register(selector, a.interestOps, a)
      }
    }
    selector.wakeup()
    future
  }

  private def finishWrite(buffer: ByteBuffer, settable: Settable[Result[Int]]): Option[Registration] = {
    set(settable) {
      channel.write(buffer)
    }.foreach(ex => dispatcher.execute(notifyListeners(ex)))
    Some(Registration(channel, selector))
  }

  private def finishRead(buffer: ByteBuffer, settable: Settable[Result[ByteBuffer]]): Option[Registration] = {
    set(settable) {
      val num = channel.read(buffer)
      if (num < 0) throw new Exception("End of stream (read -1) reached while reading")
      buffer
    }.foreach(ex => dispatcher.execute(notifyListeners(ex)))
    Some(Registration(channel, selector))
  }

}