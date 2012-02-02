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
package net.agileautomata.executor4s.testing

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import net.agileautomata.executor4s._
import java.util.concurrent.RejectedExecutionException

@RunWith(classOf[JUnitRunner])
class MockExecutorServiceTestSuite extends FunSuite with ShouldMatchers {

  test("Mock executor service fails after shutdown") {

    val executor = new InstantExecutor()
    val service = new MockExecutorService(executor)

    service.attempt(true).await should equal(Success(true))

    service.shutdown()

    intercept[RejectedExecutionException] {
      service.execute(true)
    }
  }

}