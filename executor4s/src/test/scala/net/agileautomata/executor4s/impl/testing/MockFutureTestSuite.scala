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
    val f= MockFuture.undefined[Int]
    f.listen(i => option = Some(i))
    option should equal(None)
    f.set(4)
    option should equal(Some(4))
  }

}