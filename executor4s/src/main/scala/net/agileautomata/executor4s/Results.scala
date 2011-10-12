package net.agileautomata.executor4s

object Results {
  def combine[A,B,C](f1: Future[Result[A]], f2: Future[Result[B]])(combine: (A,B) => C) : Future[Result[C]] = {
    def join(r1: Result[A], r2: Result[B]) = for(i <- r1; j <- r2) yield combine(i,j)
    Futures.combine(f1,f2)(join)
  }
}