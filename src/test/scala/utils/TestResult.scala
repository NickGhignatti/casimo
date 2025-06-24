package utils

import utils.Result.{Failure, Success}
import org.junit.Test
import org.junit.Assert.*

class TestResult {
  @Test
  def testSuccess(): Unit = {
    val result = Result.Success(42)
    assertTrue(result.isSuccess)
    assertFalse(result.isFailure)
    assertEquals(
      42,
      result match
        case Success(value) => value
        case Failure(_)     => fail("Expected Success, but got Failure")
    )
  }

  @Test
  def testFailure(): Unit = {
    val result = Result.Failure("Error occurred")
    assertFalse(result.isSuccess)
    assertTrue(result.isFailure)
    assertEquals(
      "Error occurred",
      result match
        case Success(_)     => fail("Expected Failure, but got Success")
        case Failure(error) => error
    )
  }

  @Test
  def testMap(): Unit = {
    val successResult = Result.Success(10)
    val mappedResult = successResult.map(_ * 2)
    assertEquals(Result.Success(20), mappedResult)
  }

  @Test
  def testFlatMap(): Unit = {
    val successResult = Result.Success(5)
    val flatMappedResult = successResult.flatMap(x => Result.Success(x + 5))
    assertEquals(Result.Success(10), flatMappedResult)
  }
}
