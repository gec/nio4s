package net.agileautomata.commons.testing

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
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class SynchronizedListTestSuite extends FunSuite with ShouldMatchers {

  test("Become/Remain behave as expected") {
    val num = new SynchronizedList[Int]

    onAnotherThread {
      num.append(2)
      onAnotherThread {
        num.prepend(1)
        onAnotherThread(num.append(3))
      }
    }

    num shouldBecome (1, 2, 3) within 5000
    num shouldRemain (1, 2, 3) during 500
    num.get should equal(List(1,2,3))
  }

}