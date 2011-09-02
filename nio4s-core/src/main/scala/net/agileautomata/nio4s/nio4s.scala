/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata

import java.net.InetSocketAddress

package object nio4s {
  def localhost(port: Int) = new InetSocketAddress("127.0.0.1", port)
}