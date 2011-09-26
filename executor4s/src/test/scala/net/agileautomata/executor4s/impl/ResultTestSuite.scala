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
import net.agileautomata.executor4s.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class ResultTestSuite extends FunSuite with ShouldMatchers {

  trait Foo
  object FooBar extends Foo

  test("Success is covariant in type A") {
    val x : Success[Foo] = Success(FooBar)
  }

  test("Success correctly identifies itself") {
    val x = Success(4)
    x.isSuccess should equal(true)
    x.isFailure should equal(false)
  }

  test("Failure factory method can be applied to stirng") {
    val f = Failure("foobar")
    f.isFailure should equal(true)
    f.isSuccess should equal(false)
  }

}

