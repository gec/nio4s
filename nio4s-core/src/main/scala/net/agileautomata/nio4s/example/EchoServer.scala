package net.agileautomata.nio4s.example

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
import com.typesafe.scalalogging.slf4j.Logging
import net.agileautomata.nio4s._
import java.nio.ByteBuffer
import net.agileautomata.nio4s.impl.tcp.{ TcpBinder, TcpAcceptor }
import net.agileautomata.executor4s._

trait Stoppable {
  def stop()
}

object EchoServer extends Logging {

  def main(args: Array[String]) {
    IoService.run(s => start(s.createTcpBinder, 50000))
  }

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

  private def listen(acceptor: TcpAcceptor): Unit = acceptor.accept().listen { result =>
    result match {
      case Success(channel) =>
        listen(acceptor)
        channel.read(ByteBuffer.allocateDirect(8192)).listen(onReadResult(channel))
      case Failure(ex) =>
        logger.error("Error listening", ex)
        acceptor.close()
    }
  }

  def start(binder: TcpBinder, port: Int): Stoppable = {
    val acceptor = binder.bind(port).get
    listen(acceptor)
    new Stoppable { def stop() = acceptor.close() }
  }

}