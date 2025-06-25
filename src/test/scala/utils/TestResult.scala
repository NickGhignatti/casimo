package utils

import org.scalatest.funsuite.AnyFunSuite
import utils.Result.{Failure, Success}

class TestResult extends AnyFunSuite {

  test("Success should be recognized as success") {
    val result = Success(42)
    assert(result.isSuccess)
    assert(!result.isFailure)

    result match {
      case Success(value) => assert(value == 42)
      case Failure(_)     => fail("Expected Success, but got Failure")
    }
  }

  test("Failure should be recognized as failure") {
    val result = Failure("Error occurred")
    assert(!result.isSuccess)
    assert(result.isFailure)

    result match {
      case Success(_)     => fail("Expected Failure, but got Success")
      case Failure(error) => assert(error == "Error occurred")
    }
  }

  test("map should transform Success value") {
    val successResult = Success(10)
    val mappedResult = successResult.map(_ * 2)
    assert(mappedResult == Success(20))
  }

  test("flatMap should chain computations for Success") {
    val successResult = Success(5)
    val flatMappedResult = successResult.flatMap(x => Success(x + 5))
    assert(flatMappedResult == Success(10))
  }

  test("getOrElse should return value for Success") {
    val successResult = Success(100)
    assert(successResult.getOrElse(0) == 100)
  }

  test("getOrElse should return default for Failure") {
    val failureResult = Failure("Something went wrong")
    assert(failureResult.getOrElse(42) == 42)
  }

}
