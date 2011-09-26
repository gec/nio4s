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
package net.agileautomata.executor4s.impl.testing

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import net.agileautomata.executor4s.testing.MockFuture

@RunWith(classOf[JUnitRunner])
class MockFutureTestSuite extends FunSuite with ShouldMatchers {

  test("Mock future can be constructed with value already set") {
    val mf = MockFuture.defined(42)
    mf.await should equal(42)
  }

  test("Mock future can be constructed with value undefined and then set") {
    val mf = MockFuture.undefined[Int]
    mf.set(42)
    mf.await should equal(42)
  }

  test("Awaiting and unset value throws an exception") {
    intercept[Exception](MockFuture.undefined[Int].await)
  }

  test("Map works as expected") {
    MockFuture.defined(4).map(_.toString).await should equal("4")
  }

  test("Throws if value is set twice") {
    intercept[Exception](MockFuture.defined(4).set(4))
  }

  test("Listen call immediately if value is set") {
    var option: Option[Int] = None
    MockFuture.defined(4).listen(i => option = Some(i))
    option should equal(Some(4))
  }

  test("Listen causes callback to be defered if value is unset") {
    var option: Option[Int] = None
    val f = MockFuture.undefined[Int]
    f.listen(i => option = Some(i))
    option should equal(None)
    f.set(4)
    option should equal(Some(4))
  }

}