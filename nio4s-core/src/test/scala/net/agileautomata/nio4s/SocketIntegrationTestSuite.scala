/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s

/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */

import channels.tcp.ClientSocketConnector
import example.EchoServer
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import net.agileautomata.commons.testing._
import util.Random

import net.agileautomata.executor4s._

@RunWith(classOf[JUnitRunner])
class SocketIntegrationTestSuite extends FunSuite with ShouldMatchers {

  test("Echo server handles one connection at a time") {

    NioServiceFixture { service =>

      val echo = EchoServer.start(service.server, 50000)

      val size = 3
      val bytes = new Array[Byte](size)
      Random.nextBytes(bytes)

      def testReadWrite(channel: ClientSocketConnector) = {

        val channel = service.client.connect(new InetSocketAddress("127.0.0.1", 50000)).await()()
        channel.write(ByteBuffer.wrap(bytes)).await()() should equal(size)

        val list = new SynchronizedList[Byte]
        def onRead(result: Result[ByteBuffer]) = result match {
          case Success(buff) =>
            buff.flip()
            val read = new Array[Byte](buff.remaining())
            buff.get(read)
            list.append(read)
          case Failure(ex) =>
            ex.printStackTrace()
        }

        channel.read(ByteBuffer.allocateDirect(size)).listen(onRead)
        list shouldEqual (bytes.toList.reverse) within (5000)
        channel.close()
      }

      4.times {
        testReadWrite(service.client)
      }

      echo.stop()
    }
  }

}