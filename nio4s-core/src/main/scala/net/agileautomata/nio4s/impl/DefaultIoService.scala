package net.agileautomata.nio4s.impl

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
import com.weiglewilczek.slf4s.Logging
import annotation.tailrec
import java.nio.channels.{ SelectionKey, Selector }
import java.util.{ Iterator => JavaIterator }
import net.agileautomata.executor4s._
import net.agileautomata.nio4s.impl.tcp.{ TcpBinder, TcpConnector }
import java.util.concurrent.RejectedExecutionException
import net.agileautomata.nio4s.IoService

private final class DefaultIoService extends IoService with Logging {

  def getExecutor: Executor = dispatcher
  def createStrand: Strand = Strand(dispatcher)

  def shutdown() = multiplexer.synchronized {
    multiplexer.execute {
      selector.close()
    }
    selector.wakeup()
    multiplexer.terminate()
    dispatcher.terminate()
  }

  def createTcpConnector: TcpConnector = new TcpConnector(selector, multiplexer, dispatcher)
  def createTcpConnector(strand: Strand): TcpConnector = new TcpConnector(selector, multiplexer, strand)
  def createTcpBinder: TcpBinder = new TcpBinder(selector, multiplexer, dispatcher)

  private val multiplexer = Executors.newScheduledSingleThread()
  private val selector = Selector.open()
  private val dispatcher = Executors.newScheduledThreadPool()

  multiplexer.execute(saferun())

  private def saferun(): Unit = {
    try {
      run()
    } catch {
      case ex: Exception =>
        logger.error("Unhandled exception", ex)
    }
  }

  private def run(): Unit = {

    @tailrec
    def process(keys: JavaIterator[SelectionKey]): Unit = {
      if (keys.hasNext) {
        val key = keys.next()
        keys.remove()
        key.attachment().asInstanceOf[Attachment].process(key)
        process(keys)
      }
    }

    if (selector.isOpen) {
      val num = this.selector.select()
      process(selector.selectedKeys().iterator())
      try {
        multiplexer.execute(saferun())
      } catch {
        case exs: RejectedExecutionException =>
          logger.info("Executor shutdown detected. Terminating select() loop")
      }
    }

  }

}