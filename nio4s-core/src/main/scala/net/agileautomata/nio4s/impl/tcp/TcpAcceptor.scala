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
import net.agileautomata.nio4s._
import net.agileautomata.executor4s._
import com.typesafe.scalalogging.slf4j.Logging

import java.nio.channels.{ Selector, ServerSocketChannel => NioServerSocketChannel }
import net.agileautomata.nio4s.impl.{ Attachment, Registration }

class TcpAcceptor(channel: NioServerSocketChannel, selector: Selector, multiplexer: Executor, dispatcher: Executor) extends Logging {

  channel.configureBlocking(false)

  def close() = {
    multiplexer.execute {
      logger.info("closing acceptor")
      channel.close()
    }
    selector.wakeup()
  }

  private def finishAccept(settable: Settable[Result[Channel]]): Option[Registration] = {
    try {
      Option(channel.accept()) match {
        case Some(ch) =>
          ch.configureBlocking(false)
          settable.set(Success(new TcpChannel(ch, selector, multiplexer, dispatcher)))
        case None =>
          throw new Exception("No socket to accept")
      }
    } catch {
      case ex: Exception => settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

  def accept(): Future[Result[Channel]] = {
    val future = dispatcher.future[Result[Channel]]
    multiplexer.execute {
      setOnException(future) {
        val a = Attachment(channel, selector).registerAccept(finishAccept(future))
        channel.register(selector, a.interestOps, a)
      }
    }
    selector.wakeup()
    future
  }

}