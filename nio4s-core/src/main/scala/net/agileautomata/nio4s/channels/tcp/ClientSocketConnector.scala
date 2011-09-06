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

/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */

import java.nio.channels.{ Selector, SocketChannel => NioSocketChannel }
import net.agileautomata.nio4s.api._
import net.agileautomata.executor4s._
import impl.DefaultFuture
import java.net.{ InetSocketAddress, SocketAddress }

class ClientSocketConnector(selector: Selector, multiplexer: Executor, dispatcher: Executor) {

  val socket = NioSocketChannel.open()
  socket.configureBlocking(false)

  private def finishConnect(settable: Settable[Result[Channel]]): Option[Registration] = {
    try {
      socket.finishConnect()
      settable.set(Success(new SocketChannel(socket, selector, multiplexer, dispatcher)))
      Some(Registration(socket, selector))
    } catch {
      case ex: Exception =>
        settable.set(Failure(ex))
        None
    }

  }

  def connect(host: String, port: Int): Future[Result[Channel]] = connect(new InetSocketAddress(host, port))

  def connect(addr: SocketAddress): Future[Result[Channel]] = {
    val promise = new DefaultFuture[Result[Channel]](dispatcher)
    multiplexer.set(promise) {
      socket.connect(addr)
      val a = Attachment(socket, selector).registerConnect(finishConnect(promise))
      socket.register(selector, a.interestOps, a)
    }
    selector.wakeup()
    promise
  }

}