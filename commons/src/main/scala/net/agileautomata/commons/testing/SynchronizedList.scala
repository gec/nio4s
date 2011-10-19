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

class SynchronizedList[A] {

  val value = new SynchronizedVariable[List[A]](Nil)

  def get = value.get
  def append(a: A) = value.modify(_ ::: List(a))
  def prepend(a: A) = value.modify(a :: _)

  def shouldBecome(list: List[A]): Within = value.shouldBecome(list)
  def shouldBecome(values: A*): Within = shouldBecome(values.toList)
  def shouldRemain(list: List[A]): During = value.shouldRemain(list)
  def shouldRemain(values: A*): During = shouldRemain(values.toList)

}