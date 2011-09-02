/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.nio4s.api

import java.nio.channels.{ SelectableChannel, Selector }

object Registration {
  def apply(channel: SelectableChannel, selector: Selector) = new BasicRegistration(channel, selector)
}

trait Registration {
  def apply(a: Attachment)
}

class BasicRegistration(channel: SelectableChannel, selector: Selector) extends Registration {
  def apply(a: Attachment) = channel.register(selector, a.interestOps, a)
}