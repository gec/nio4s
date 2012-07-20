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
import net.agileautomata.executor4s.{ TimeInterval, Failure, Success, Result }
import java.net.SocketAddress

package object dsl {

  implicit def decorateAsynchronousServerSocketChannel(channel: AsynchronousServerSocketChannel) =
    new AsynchronousServerSocketChannelDecorator(channel)

  implicit def decorateAsynchronousSocketChannel(channel: AsynchronousSocketChannel) =
    new AsynchronousSocketChannelDecorator(channel)
}

object ExceptionHandler {

  def reportExceptions[A](callback: Result[A] => Unit)(fun: => Unit): Unit = {
    try {
      fun
    } catch {
      case ex: Exception => callback(Failure(ex))
    }
  }
}

class AsynchronousServerSocketChannelDecorator(channel: AsynchronousServerSocketChannel) {

  import ExceptionHandler._

  def acceptAsync(callback: Result[AsynchronousSocketChannel] => Unit): Unit = {
    val handler = new CompletionHandler[AsynchronousSocketChannel, Void] {
      def completed(result: AsynchronousSocketChannel, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    reportExceptions(callback)(channel.accept(null, handler))
  }

}

class AsynchronousSocketChannelDecorator(channel: AsynchronousSocketChannel) {

  import ExceptionHandler._

  def connectAsync(address: SocketAddress)(callback: Result[AsynchronousSocketChannel] => Unit): Unit = {
    val handler = new CompletionHandler[Void, Void] {
      def completed(result: Void, attachment: Void) = callback(Success(channel))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    reportExceptions(callback)(channel.connect(address, null, handler))
  }

  def writeAsyncOnce(src: ByteBuffer, timeout: Option[TimeInterval])(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    timeout match {
      case Some(to) => reportExceptions(callback)(channel.write(src, to.count, to.timeunit, null, handler))
      case None => reportExceptions(callback)(channel.write(src, null, handler))
    }
  }

  def readAsyncOnce(dst: ByteBuffer, timeout: Option[TimeInterval])(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    timeout match {
      case Some(to) => reportExceptions(callback)(channel.read(dst, to.count, to.timeunit, null, handler))
      case None => reportExceptions(callback)(channel.read(dst, null, handler))
    }

  }

  def writeAsyncAll(src: ByteBuffer, timeout: Option[TimeInterval])(callback: Result[ByteBuffer] => Unit): Unit = {
    def onWrite(res: Result[Int]): Unit = res match {
      case Success(x) =>
        if (src.remaining() > 0) writeAsyncOnce(src, timeout)(onWrite)
        else callback(Success(src))
      case f: Failure => callback(f)
    }
    writeAsyncOnce(src, timeout)(onWrite)
  }

  def readAsyncAll(src: ByteBuffer, timeout: Option[TimeInterval])(callback: Result[ByteBuffer] => Unit): Unit = {
    require(src.remaining() > 0)
    def onRead(res: Result[Int]): Unit = res match {
      case Success(num) =>
        if (num <= 0) callback(Failure("EOS"))
        else if (src.remaining() > 0) readAsyncOnce(src, timeout)(onRead)
        else callback(Success(src))
      case f: Failure => callback(f)
    }
    readAsyncOnce(src, timeout)(onRead)
  }

}

