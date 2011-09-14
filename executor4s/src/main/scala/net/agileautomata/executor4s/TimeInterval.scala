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

sealed trait TimeInterval {
  def timeunit: TimeUnit
  def count: Long
}

sealed abstract class DefaultTimeInterval(num: Long, unit: TimeUnit) extends TimeInterval {
  override def count = num
  override def timeunit = unit

  override def toString: String = num + " " + unit.toString
}

final case class NanoSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.NANOSECONDS)
final case class MicroSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MICROSECONDS)
final case class MilliSeconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.MILLISECONDS)
final case class Seconds(num: Long) extends DefaultTimeInterval(num, TimeUnit.SECONDS)
final case class Minutes(num: Long) extends DefaultTimeInterval(num, TimeUnit.DAYS)
final case class Hours(num: Long) extends DefaultTimeInterval(num, TimeUnit.HOURS)
final case class Days(num: Long) extends DefaultTimeInterval(num, TimeUnit.DAYS)

