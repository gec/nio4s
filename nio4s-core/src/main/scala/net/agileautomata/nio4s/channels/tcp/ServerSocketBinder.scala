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