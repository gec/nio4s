package net.agileautomata.nio4s.impl.tcp

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
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import net.agileautomata.nio4s._
import java.io.IOException
import net.agileautomata.commons.testing._
import java.nio.ByteBuffer

@RunWith(classOf[JUnitRunner])
class TcpTestSuite extends FunSuite with ShouldMatchers {

  def pair(fun: (Channel, Channel) => Unit) = NioServiceFixture { service =>
    val binder = service.createTcpBinder
    val acceptor = binder.bind(50000).apply()
    val client = service.createTcpConnector.connect(localhost(50000)).await()
    val server = acceptor.accept().await()
    try {
      fun(client, server)
    } finally {
      acceptor.close()
    }
  }

  test("connection is rejected with no listener bound") {
    NioServiceFixture { service =>
      intercept[IOException] {
        service.createTcpConnector.connect(localhost(50000)).await()
      }
    }
  }

  test("connection is accepted with listener bound") {
    NioServiceFixture { service =>
      val acceptor = service.createTcpBinder.bind(50000).apply()
      service.createTcpConnector.connect(localhost(50000)).await()
      acceptor.close()
    }
  }

  test("Exceptions are reported to listener on read error") {
    NioServiceFixture { service =>
      val exceptions = new SynchronizedList[Exception]
      val acceptor = service.createTcpBinder.bind(50000).apply()
      val client = service.createTcpConnector.connect(localhost(50000)).await()
      val server = acceptor.accept().await()
      acceptor.close()
      client.listen(exceptions.append)
      val future = client.read(ByteBuffer.allocateDirect(1))
      server.close()
      future.await.isFailure should be(true)
      exceptions shouldBeOfSize (1) within (5000)
    }
  }

  def testReadFailureOnClose(c1: Channel, c2: Channel) {
    val future = c1.read(ByteBuffer.allocateDirect(1))
    c2.close()
    future.await.isFailure should be(true)
    c1.close()
  }

  test("Client close while server reading yields failure") {
    pair((client, server) => testReadFailureOnClose(server, client))
  }

  test("Server close while client reading yields failure") {
    pair((client, server) => testReadFailureOnClose(client, server))
  }

  test("Double read yields error") {
    pair { (client, server) =>
      val buffer = ByteBuffer.allocateDirect(1)
      val f1 = client.read(buffer)
      val f2 = client.read(buffer)
      f2.await.isFailure should be(true)
    }
  }

  test("Multiple connections can be established") {
    NioServiceFixture { service =>

      val accepts = new SynchronizedList[Boolean]
      val connects = new SynchronizedList[Boolean]
      val server = service.createTcpBinder
      val acceptor = server.bind(50000).apply()

      def accept(a: TcpAcceptor): Unit = a.accept.listen { rsp =>
        accepts.append(rsp.isSuccess)
        if (rsp.isSuccess) accept(a)
      }

      accept(acceptor)

      3.times {
        connects.append(service.createTcpConnector.connect(localhost(50000)).await.isSuccess)
      }

      accepts shouldEqual (true, true, true) within (5000)
      connects shouldEqual (true, true, true) within (5000)

      acceptor.close()
    }
  }

}