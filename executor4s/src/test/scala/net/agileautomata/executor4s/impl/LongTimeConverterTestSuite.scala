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
package net.agileautomata.executor4s.impl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

import net.agileautomata.executor4s._

@RunWith(classOf[JUnitRunner])
class LongTimeConverterTestSuite extends FunSuite with ShouldMatchers {
  def testTimeType(unit: TimeUnit)(gen: Long => TimeInterval) = {
    gen(4).count should equal(4)
    gen(2).timeunit should equal(unit)
  }

  test("Implicit time conversions work as expected") {
    testTimeType(TimeUnit.NANOSECONDS)(_.nanoseconds)
    testTimeType(TimeUnit.MICROSECONDS)(_.microseconds)
    testTimeType(TimeUnit.MILLISECONDS)(_.milliseconds)
    testTimeType(TimeUnit.SECONDS)(_.seconds)
    testTimeType(TimeUnit.MINUTES)(_.minutes)
    testTimeType(TimeUnit.HOURS)(_.hours)
    testTimeType(TimeUnit.DAYS)(_.days)

  }
}