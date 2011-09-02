/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s.example

import com.weiglewilczek.slf4s.Logging
import net.agileautomata.nio4s.api._
import java.nio.ByteBuffer
import net.agileautomata.nio4s.channels.tcp.{ ServerSocketBinder, ServerSocketAcceptor }
import net.agileautomata.executor4s._

trait Stoppable {
  def stop()
}

object EchoServer extends Logging {

  def main(args: Array[String]) = IoService.run(s => start(s.server, 50000))

  private def onWriteResult(channel: Channel, buff: ByteBuffer)(r: Result[Int]): Unit = r match {
    case Success(num) =>
      buff.clear()
      channel.read(buff).listen(onReadResult(channel))
    case Failure(ex) =>
      logger.error("Error writing", ex)
      channel.close()
  }

  private def onReadResult(channel: Channel)(r: Result[ByteBuffer]): Unit = r match {
    case Success(buff) =>
      buff.flip()
      channel.write(buff).listen(onWriteResult(channel, buff))
    case Failure(ex) =>
      channel.close()
  }

  private def listen(acceptor: ServerSocketAcceptor): Unit = acceptor.accept().listen { result =>
    result match {
      case Success(channel) =>
        listen(acceptor)
        channel.read(ByteBuffer.allocateDirect(8192)).listen(onReadResult(channel))
      case Failure(ex) =>
        logger.error("Error listening", ex)
        acceptor.close()
    }
  }

  def start(binder: ServerSocketBinder, port: Int): Stoppable = {
    val acceptor = binder.bind(port).apply()
    listen(acceptor)
    new Stoppable { def stop() = acceptor.close() }
  }

}