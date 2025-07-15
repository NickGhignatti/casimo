package model.entities.customers

import org.scalatest.funsuite.AnyFunSuite

case class MockBF(boredom: Double, frustration: Double)
    extends BoredomFrustration[MockBF]:

  protected def updatedBoredom(newBoredom: Double): MockBF =
    this.copy(boredom = newBoredom)

  protected def updatedFrustration(newFrustration: Double): MockBF =
    this.copy(frustration = newFrustration)

class TestBoredomFrustration extends AnyFunSuite:

  test(
    "creating a class with a legal Boredom or Frustration format should store the amount"
  ):
    val mock = MockBF(boredom = 70.0, frustration = 20.0)
    assert(mock.boredom === 70.0)
    assert(mock.frustration === 20.0)

  test(
    "creating a class with a non percentile value of boredom or frustration should throw IllegalArgumentException"
  ):
    val negativeValue = -50.0
    val exB = intercept[IllegalArgumentException] {
      MockBF(boredom = negativeValue, frustration = 30.0)
    }
    val exF = intercept[IllegalArgumentException] {
      MockBF(boredom = 30.0, frustration = negativeValue)
    }
    assert(
      exB.getMessage === s"requirement failed: Boredom must be a percentile value, instead is $negativeValue %"
    )
    assert(
      exF.getMessage === s"requirement failed: Frustration must be a percentile value, instead is $negativeValue %"
    )

  test(
    "Modifying the boredom or frustration to a new valid amount should update the amount"
  ):
    val mock = MockBF(boredom = 50, frustration = 70)
    val updatedMock = mock.updateBoredom(-20.0)
    val updatedMock2 = mock.updateFrustration(20.0)
    assert(updatedMock.boredom === 50.0 - 20.0)
    assert(updatedMock2.frustration === 70.0 + 20.0)

  test(
    "Modifying the Boredom or frustration to an invalid amount should clip to the max or min value"
  ):
    val startValue = 90.0
    val mock = MockBF(boredom = startValue, frustration = startValue)
    val netChange = 100.0
    val updatedMock = mock.updateBoredom(netChange)
    val updatedMock2 = updatedMock.updateFrustration(-netChange)
    assert(updatedMock.boredom === 100.0)
    assert(updatedMock2.frustration === 0.0)
