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
package net.agileautomata.nio4s.example

import java.nio.channels._
import net.agileautomata.executor4s._
import java.net.InetSocketAddress
import java.nio.ByteBuffer

import net.agileautomata.nio4s.dsl._

object EchoServer extends App {

  private val pool = java.util.concurrent.Executors.newScheduledThreadPool(4)
  private val group = AsynchronousChannelGroup.withThreadPool(pool)
  private val server = AsynchronousServerSocketChannel.open(group)
  private val address = new InetSocketAddress("127.0.0.1", 20000)

  private def onWrite(socket: AsynchronousSocketChannel, buffer: ByteBuffer)(result: Result[Int]): Unit = result match {
    case Failure(ex) =>
      socket.close()
    case Success(num) =>
      println("Wrote: " + num)
      buffer.clear()
      socket.readAsync(buffer)(onRead(socket, buffer))
  }

  private def onRead(socket: AsynchronousSocketChannel, buff: ByteBuffer)(result: Result[Int]): Unit = result match {
    case Failure(ex) =>
      socket.close()
    case Success(num) =>
      if (num > 0) {
        buff.flip()
        socket.writeAsync(buff)(onWrite(socket, buff))
      } else socket.close()
  }

  private def onAccept(result: Result[AsynchronousSocketChannel]): Unit = result match {
    case Failure(ex) =>
    // TODO - logging
    case Success(channel) =>
      server.acceptAsync(onAccept _)
      val buffer = ByteBuffer.allocateDirect(1024)
      channel.readAsync(buffer)(onRead(channel, buffer))
  }

  private val s = server.bind(address).acceptAsync(onAccept _)

  readLine()
  println("Shutting down...")
  group.shutdown()
  pool.shutdown()

}