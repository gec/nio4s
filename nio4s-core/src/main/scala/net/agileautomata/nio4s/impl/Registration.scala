package net.agileautomata.nio4s.impl

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
import java.nio.channels.{ SelectableChannel, Selector }

private object Registration {
  def apply(channel: SelectableChannel, selector: Selector) = new BasicRegistration(channel, selector)
}

private trait Registration {
  def apply(a: Attachment)
}

private class BasicRegistration(channel: SelectableChannel, selector: Selector) extends Registration {
  def apply(a: Attachment) = channel.register(selector, a.interestOps, a)
}