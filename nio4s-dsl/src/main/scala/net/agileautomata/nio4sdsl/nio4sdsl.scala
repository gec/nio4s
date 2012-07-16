package net.agileautomata.nio4sdsl

import java.nio.channels._
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import net.agileautomata.executor4s._

class AsynchronousServerSocketChannelDecorator(channel: AsynchronousServerSocketChannel) {

  def acceptAsync(callback: Result[AsynchronousSocketChannel] => Unit): Unit  = {
    val handler = new CompletionHandler[AsynchronousSocketChannel, Void] {
      def completed(result: AsynchronousSocketChannel, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.accept(null, handler)
  }

  def acceptWithFuture(executor: Executor) : Future[Result[AsynchronousSocketChannel]] = {
    val future = executor.future[Result[AsynchronousSocketChannel]]
    this.acceptAsync(future.set)
    future
  }

}

class AsynchronousSocketChannelDecorator(channel: AsynchronousSocketChannel) {

  def writeAsync(src: ByteBuffer)(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.write(src, null, handler)
  }

  def writeWithFuture(executor: Executor, buffer: ByteBuffer): Future[Result[Int]] = {
    val future = executor.future[Result[Int]]
    this.writeAsync(buffer)(future.set)
    future
  }

  def readAsync(dst: ByteBuffer)(callback: Result[Int] => Unit): Unit = {
    val handler = new CompletionHandler[java.lang.Integer, Void] {
      def completed(result: java.lang.Integer, attachment: Void) = callback(Success(result))
      def failed(exc: Throwable, attachment: Void) = callback(Failure(exc))
    }
    channel.read(dst, null, handler)
  }

  def readWithFuture(executor: Executor, buffer: ByteBuffer): Future[Result[Int]] = {
    val future = executor.future[Result[Int]]
    this.readAsync(buffer)(future.set)
    future
  }

}

object nio4sdsl extends App {

  implicit def decorateAsynchronousServerSocketChannel(channel: AsynchronousServerSocketChannel) =
    new AsynchronousServerSocketChannelDecorator(channel)

  implicit def decorateAsynchronousSocketChannel(channel: AsynchronousSocketChannel) =
    new AsynchronousSocketChannelDecorator(channel)

  val pool = java.util.concurrent.Executors.newScheduledThreadPool(4)
  val exe = Executors.newCustomExecutor(pool, TimeInterval.EndOfTheUniverse)
  val group = AsynchronousChannelGroup.withThreadPool(pool)
  val server = AsynchronousServerSocketChannel.open(group)
  val address = new InetSocketAddress("127.0.0.1", 20000)

  def onWrite(socket: AsynchronousSocketChannel, buffer: ByteBuffer)(result: Result[Int]): Unit = result match {
    case Failure(ex) =>
      socket.close()
    case Success(num) =>
      println("Wrote: " + num)
      buffer.clear()
      socket.readAsync(buffer)(onRead(socket, buffer))
  }

  def onRead(socket: AsynchronousSocketChannel, buff: ByteBuffer)(result: Result[Int]): Unit = result match {
    case Failure(ex) =>
      socket.close()
    case Success(num) =>
      buff.flip()
      socket.writeAsync(buff)(onWrite(socket, buff))
  }

  def onAccept(result: Result[AsynchronousSocketChannel]): Unit = result match {
    case Failure(ex) =>
      // TODO - logging
    case Success(channel) =>
      server.acceptAsync(onAccept _)
      val buffer = ByteBuffer.allocateDirect(1024)
      channel.readAsync(buffer)(onRead(channel, buffer))
  }


  val s = server.bind(address).acceptAsync(onAccept _)

  Thread.sleep(60000)

}
