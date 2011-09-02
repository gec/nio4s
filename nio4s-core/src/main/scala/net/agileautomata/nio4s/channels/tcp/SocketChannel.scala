/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s.channels.tcp

/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
import java.nio.ByteBuffer

import net.agileautomata.nio4s.api._
import java.nio.channels.{ Selector, SelectionKey, SocketChannel => NioSocketChannel }
import net.agileautomata.executor4s._
import impl.DefaultFuture

final class SocketChannel(channel: NioSocketChannel, selector: Selector, multiplexer: Executor, dispatcher: Executor) extends Channel {

  def isOpen = channel.isOpen()

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
      case ex: Exception => settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

  private def finishRead(buffer: ByteBuffer, settable: Settable[Result[ByteBuffer]]): Option[Registration] = {
    try {
      val num = channel.read(buffer)
      if (num < 0) settable.set(Failure(new Exception("end of stream")))
      else settable.set(Success(buffer))
    } catch {
      case ex: Exception => settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

}