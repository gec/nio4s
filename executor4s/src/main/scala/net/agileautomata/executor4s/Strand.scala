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

/*
 *  A strand is an executor that guarantees operations
 *  are not executed concurrently. It makes no guarantees
 *  about ordering of submitted tasks.
 */
trait Strand extends Executor {

  // terminate the Strand, executing one last function
  // when terminate returns, the execution is complete
  // and no more tasks will run on this strand
  def terminate(fun: => Unit): Unit

  // terminate the strand, when terminate
  def terminate(): Unit
}

object Strand {

  def apply(exe: Executor): Strand = exe match {
    case s: Strand => s // don't re-wrap strands
    case e: Executor => new impl.StrandExecutorWrapper(exe)
  }
}