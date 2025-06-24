package utils

import utils.Result.{Failure, Success}
import org.junit.Test
import org.junit.Assert.*

class TestVector2D {
  @Test
  def testAddition(): Unit = {
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(3.0, 4.0)
    val result = v1 + v2
    assertEquals(Vector2D(4.0, 6.0), result)
  }

  @Test
  def testSubtraction(): Unit = {
    val v1 = Vector2D(5.0, 7.0)
    val v2 = Vector2D(2.0, 3.0)
    val result = v1 - v2
    assertEquals(Vector2D(3.0, 4.0), result)
  }

  @Test
  def testMoltiplication(): Unit = {
    val v = Vector2D(2.0, 3.0)
    val scalar = 4.0
    val result = v * scalar
    assertEquals(Vector2D(8.0, 12.0), result)
  }

  @Test
  def testDivision(): Unit = {
    val v = Vector2D(8.0, 12.0)
    val scalar = 4.0
    val result = v / scalar
    assertEquals(Vector2D(2.0, 3.0), result)
  }

  @Test
  def testDotProduct(): Unit = {
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(3.0, 4.0)
    val result = v1 dot v2
    assertEquals(11.0, result, 0.0001)
  }

  @Test
  def testNormalization(): Unit = {
    val v = Vector2D(3.0, 4.0)
    val normalized = v.normalize
    assertEquals(Vector2D(0.6, 0.8), normalized)
  }

  @Test
  def testEquality(): Unit = {
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(1.0, 2.0)
    assertEquals(v1, v2)
  }
}
