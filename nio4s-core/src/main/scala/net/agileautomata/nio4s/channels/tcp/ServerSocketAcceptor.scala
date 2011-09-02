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

import net.agileautomata.nio4s.api._
import net.agileautomata.executor4s._
import net.agileautomata.executor4s.impl.DefaultFuture
import com.weiglewilczek.slf4s.Logging

import java.nio.channels.{ Selector, ServerSocketChannel => NioServerSocketChannel }

class ServerSocketAcceptor(channel: NioServerSocketChannel, selector: Selector, multiplexer: Executor, dispatcher: Executor) extends Logging {

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
          settable.set(Success(new SocketChannel(ch, selector, multiplexer, dispatcher)))
        case None =>
          logger.error("No socket to accept")
      }
    } catch {
      case ex: Exception => settable.set(Failure(ex))
    }
    Some(Registration(channel, selector))
  }

  def accept(): Future[Result[Channel]] = {
    val promise = new DefaultFuture[Result[Channel]](dispatcher)
    multiplexer.set(promise) {
      val a = Attachment(channel, selector).registerAccept(finishAccept(promise))
      channel.register(selector, a.interestOps, a)
    }
    selector.wakeup()
    promise
  }

}