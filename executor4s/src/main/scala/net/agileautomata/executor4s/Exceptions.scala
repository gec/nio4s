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

/**
 * These exceptions should only occur if the system has an unexpected deadlock. They are thrown primarily to provide
 * stack traces of deadlock locations.
 */
abstract class Executor4sTimeoutException(message: String) extends Exception(message)

/**
 * thrown if future.await times out
 */
final class AwaitTimeoutException(interval: TimeInterval) extends Executor4sTimeoutException("Await timed out after " + interval.nanosec + " nanoseconds")

/**
 * thrown if timer.cancel times out
 */
final class CancelTimeoutException(interval: TimeInterval) extends Executor4sTimeoutException("Timer cancel timed out after " + interval.nanosec + " nanoseconds")