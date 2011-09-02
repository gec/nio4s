/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s

import java.util.concurrent.TimeUnit

sealed trait TimeInterval {
  def timeunit: TimeUnit
  def count: Long
}

sealed abstract class DefaultTimeInterval(num: Long, unit: TimeUnit) extends TimeInterval {
  override def count = num
  override def timeunit = unit
}

final case class NanoSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.NANOSECONDS)
final case class MicroSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MICROSECONDS)
final case class MilliSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MILLISECONDS)
final case class Seconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.SECONDS)
final case class Minutes(num: Long) extends DefaultTimeInterval(num, TimeUnit.DAYS)
final case class Hours(num: Long) extends DefaultTimeInterval(num, TimeUnit.HOURS)
final case class Days(num: Long) extends DefaultTimeInterval(num, TimeUnit.DAYS)

