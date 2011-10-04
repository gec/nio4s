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
package net.agileautomata.executor4s

import java.util.concurrent.TimeUnit

object TimeInterval {
  val microToNano: Long = 1000
  val milliToNano: Long = microToNano * 1000
  val secToNano: Long = milliToNano * 1000
  val minutesToNano: Long = secToNano * 60
  val hoursToNano: Long = minutesToNano * 60
  val daysToNano: Long = hoursToNano * 24
}

sealed trait TimeInterval {
  def timeunit: TimeUnit
  def count: Long
  def nanosec: Long
}

sealed abstract class DefaultTimeInterval(num: Long, unit: TimeUnit, toNano: Long) extends TimeInterval {
  override def count = num
  override def timeunit = unit
  override def nanosec: Long = num * toNano

  override def toString: String = num + " " + unit.toString
}

final case class NanoSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.NANOSECONDS, 1)
final case class MicroSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MICROSECONDS, TimeInterval.microToNano)
final case class MilliSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MILLISECONDS, TimeInterval.milliToNano)
final case class Seconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.SECONDS, TimeInterval.secToNano)
final case class Minutes(num: Long) extends DefaultTimeInterval(num, TimeUnit.MINUTES, TimeInterval.minutesToNano)
final case class Hours(num: Long) extends DefaultTimeInterval(num, TimeUnit.HOURS, TimeInterval.hoursToNano)
final case class Days(num: Long) extends DefaultTimeInterval(num, TimeUnit.DAYS, TimeInterval.daysToNano)

