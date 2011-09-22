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

import impl.Defaults
import impl.tcp.{ TcpBinder, TcpConnector }
import net.agileautomata.executor4s.{ Strand, Executor }

object IoService {
  def apply(): IoService = Defaults.ioService

  def run[A](fun: IoService => A): A = {
    val service = IoService()
    try {
      fun(service)
    } finally {
      service.shutdown()
    }
  }
}

trait IoService {

  def getExecutor: Executor

  def createStrand: Strand

  def shutdown(): Unit

  def createTcpConnector: TcpConnector

  def createTcpConnector(strand: Strand): TcpConnector

  def createTcpBinder: TcpBinder

}