/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.executor4s.impl

import net.agileautomata.executor4s._
import java.util.concurrent.{ ScheduledExecutorService }

class FunRun(fun: => Unit) extends Runnable {
  def run() = fun
}

final class DecoratedExecutor(exe: ScheduledExecutorService) extends Callable with ExecutorService {

  override def execute(fun: => Unit): Unit = exe.execute(new FunRun(fun))

  override def shutdown() = exe.shutdown()

  override def terminate(interval: TimeInterval) = {
    shutdown()
    exe.awaitTermination(interval.count, interval.timeunit)
  }

  override def delay(interval: TimeInterval)(fun: => Unit): Cancelable = {
    val future = exe.schedule(new FunRun(fun), interval.count, interval.timeunit)
    new Cancelable {
      def cancel() { future.cancel(false) }
    }
  }
}

