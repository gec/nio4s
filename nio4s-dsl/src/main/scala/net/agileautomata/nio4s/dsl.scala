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
package net.agileautomata.nio4s

import java.nio.channels._
import java.nio.ByteBuffer
import net.agileautomata.executor4s.{ Failure, Success, Result }
import java.net.SocketAddress

package object dsl {

  implicit def decorateAsynchronousServerSocketChannel(channel: AsynchronousServerSocketChannel) =
    new AsynchronousServerSocketChannelDecorator(channel)

  implicit def decorateAsynchronousByteChannel(channel: AsynchronousByteChannel) =
    new AsynchronousByteChannelDecorator(channel)

  implicit def decorateAsynchronousSocketChannel(channel: AsynchronousSocketChannel) =
    new AsynchronousSocketChannelDecorator(channel)
}

class AsynchronousSocketChannelDecorator(channel: AsynchronousSocketChannel) {

  def connectAsync(address: SocketAddress)(callback: Result[AsynchronousSocketChannel] => Unit): Unit = {
    val handler = new CompletionHandler[Void, Void] {
      def completed(result: Void, attachment: Void) = callback(Success(channel))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.connect(address, null, handler)
  }
}

class AsynchronousServerSocketChannelDecorator(channel: AsynchronousServerSocketChannel) {

  def acceptAsync(callback: Result[AsynchronousSocketChannel] => Unit): Unit = {
    val handler = new CompletionHandler[AsynchronousSocketChannel, Void] {
      def completed(result: AsynchronousSocketChannel, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.accept(null, handler)
  }

}

class AsynchronousByteChannelDecorator(channel: AsynchronousByteChannel) {

  def writeAsyncOnce(src: ByteBuffer)(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.write(src, null, handler)
  }

  def readAsyncOnce(dst: ByteBuffer)(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.read(dst, null, handler)
  }

  def writeAsyncAll(src: ByteBuffer)(callback: Result[ByteBuffer] => Unit): Unit = {
    def onWrite(res: Result[Int]): Unit = res match {
      case Success(x) =>
        if (src.remaining() > 0) writeAsyncOnce(src)(onWrite)
        else callback(Success(src))
      case f: Failure => callback(f)
    }
    writeAsyncOnce(src)(onWrite)
  }

  def readAsyncAll(src: ByteBuffer)(callback: Result[ByteBuffer] => Unit): Unit = {
    def onRead(res: Result[Int]): Unit = res match {
      case Success(num) =>
        if (num < 0) callback(Failure("EOS"))
        else if (src.remaining() > 0) readAsyncOnce(src)(onRead)
        else callback(Success(src))
      case f: Failure => callback(f)
    }
    readAsyncOnce(src)(onRead)
  }

}

