/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s

import api.IoService

object NioServiceFixture {

  def apply[A](test: IoService => A): A = {
    val service = new IoService
    try {
      test(service)
    } finally {
      service.shutdown()
    }
  }
}