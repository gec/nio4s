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
package net.agileautomata.nio4s.api

import java.nio.channels.Selector
import java.nio.channels.spi.AbstractSelectableChannel

import java.io.IOException
import java.nio.channels.SelectionKey

object Attachment {
  type Callback = () => Option[Registration]

  def apply(channel: AbstractSelectableChannel, selector: Selector): Attachment = Option(channel.keyFor(selector)) match {
    case Some(x) => x.attachment().asInstanceOf[Attachment]
    case None => Attachment(None, None, None, None)
  }
}

import Attachment._

/**
 * Immutable class used to dispatch and register select events
 */
case class Attachment(accept: Option[Callback], connect: Option[Callback], read: Option[Callback], write: Option[Callback]) {

  /**
   * process the highest priority operation.
   *
   * @return A modified attachment or None
   */
  def process(key: SelectionKey): Unit = {

    if (key.isValid) {
      if (key.isAcceptable) onAccept
      else if (key.isConnectable) onConnect
      else if (key.isWritable) onWrite
      else if (key.isReadable) onRead
      else None
    } else None
  }

  def interestOps: Int = {
    def pAccept = if (accept.isDefined) SelectionKey.OP_ACCEPT else 0
    def pConnect = if (connect.isDefined) SelectionKey.OP_CONNECT else 0
    def pRead = if (read.isDefined) SelectionKey.OP_READ else 0
    def pWrite = if (write.isDefined) SelectionKey.OP_WRITE else 0
    pAccept | pConnect | pRead | pWrite
  }

  def registerConnect(fun: => Option[Registration]): Attachment = this.copy(connect = register("connect", connect, () => fun))

  def registerAccept(fun: => Option[Registration]): Attachment = this.copy(accept = register("accept", accept, () => fun))

  def registerRead(fun: => Option[Registration]): Attachment = this.copy(read = register("read", read, () => fun))

  def registerWrite(fun: => Option[Registration]): Attachment = this.copy(write = register("write", write, () => fun))

  private def onConnect: Unit = invoke("connect", connect, this.copy(connect = None))

  private def onAccept: Unit = invoke("accept", accept, this.copy(accept = None))

  private def onRead: Unit = invoke("read", read, this.copy(read = None))

  private def onWrite: Unit = invoke("write", write, this.copy(write = None))

  private def invoke(op: String, cb: Option[Callback], a: Attachment) = cb match {
    case Some(fun) => fun().foreach(_.apply(a))
    case None => throw new IOException("Callback not registered for operation: " + op)
  }

  private def register(op: String, cb: Option[Callback], callback: Callback): Option[Callback] = cb match {
    case Some(fun) => throw new IOException("Operation already in progress: " + op)
    case None => Some(callback)
  }

}