/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.impl

import net.agileautomata.executor4s._

final class LongTimeConverter(count: Long) {

  def nanoseconds = NanoSeconds(count)
  def microseconds = MicroSeconds(count)
  def milliseconds = MilliSeconds(count)
  def seconds = Seconds(count)
  def minutes = Minutes(count)
  def hours = Hours(count)
  def days = Days(count)

}