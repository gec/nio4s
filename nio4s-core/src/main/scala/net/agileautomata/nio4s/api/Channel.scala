/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s.api

import net.agileautomata.executor4s._

/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
import java.nio.ByteBuffer

trait Channel {

  def isOpen: Boolean

  def close(): Result[Unit]

  def read(buffer: ByteBuffer): Future[Result[ByteBuffer]]

  def write(buffer: ByteBuffer): Future[Result[Int]]

}