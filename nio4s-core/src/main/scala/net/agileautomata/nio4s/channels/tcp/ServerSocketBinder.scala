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
import java.net.InetSocketAddress
import java.nio.channels.{ Selector, ServerSocketChannel => NioServerSocketChannel }
import net.agileautomata.executor4s._

final class ServerSocketBinder(selector: Selector, multiplexer: Executor, dispatcher: Executor) {

  private val channel = NioServerSocketChannel.open()
  channel.configureBlocking(false)

  def bind(port: Int): Result[ServerSocketAcceptor] = {
    try {
      channel.socket().bind(new InetSocketAddress(port))
      Success(new ServerSocketAcceptor(channel, selector, multiplexer, dispatcher))
    } catch {
      case ex: Exception => Failure(ex)
    }
  }

}