package model.spawner

import model.entities.spawner.GaussianStrategy
import model.entities.spawner.SpawningStrategyBuilder
import model.entities.spawner.StepStrategy
import org.scalatest.funsuite.AnyFunSuite

class TestSpawningStrategy extends AnyFunSuite:

  test(
    "Constant strategy should return every time the same amount of customers"
  ):
    val basic = SpawningStrategyBuilder().constant(10).build()
    assert(basic.customersAt(1) == 10)
    assert(basic.customersAt(5) == 10)
    assert(basic.customersAt(20) == 10)

  test("GaussianStrategy should return peak value at mean time"):
    val strategy = GaussianStrategy(100, 10.0, 2.0)
    assert(strategy.customersAt(10.0) == 100)

  test("GaussianStrategy should return symmetric values around mean"):
    val strategy = GaussianStrategy(100, 10.0, 2.0)
    val left = strategy.customersAt(8.0) // mean - stdDev
    val right = strategy.customersAt(12.0) // mean + stdDev
    assert(left == right)

  test("GaussianStrategy should return decreasing values away from mean"):
    val strategy = GaussianStrategy(100, 10.0, 2.0)
    val near = strategy.customersAt(10.0)
    val mid = strategy.customersAt(14.0) // mean + 2*stdDev
    val far = strategy.customersAt(20.0) // mean + 5*stdDev

    assert(near > mid)
    assert(mid > far)
    assert(far < 1)

  test("GaussianStrategy should always return non-negative values"):
    val strategy = GaussianStrategy(100, 10.0, 2.0)
    assert(strategy.customersAt(0.0) >= 0)
    assert(strategy.customersAt(100.0) >= 0)

  test("GaussianStrategy should respect base offset"):
    val strategy =
      GaussianStrategy(100, 10.0, 2.0, 10)
    assert(strategy.customersAt(99.0) == 10)
    assert(strategy.customersAt(10.0) == 110)

  test("StepStrategy should return low rate before start time"):
    val strategy = StepStrategy(5, 20, 8.0, 18.0)
    assert(strategy.customersAt(0.0) == 5)
    assert(strategy.customersAt(7.9) == 5)

  test("StepStrategy should return high rate during active period"):
    val strategy = StepStrategy(5, 20, 8.0, 18.0)
    assert(strategy.customersAt(8.0) == 20)
    assert(strategy.customersAt(12.5) == 20)
    assert(strategy.customersAt(18.0) == 20)

  test("StepStrategy should return low rate after end time"):
    val strategy = StepStrategy(5, 20, 8.0, 18.0)
    assert(strategy.customersAt(18.1) == 5)
    assert(strategy.customersAt(100.0) == 5)

  test("StepStrategy should handle exact boundaries correctly"):
    val strategy = StepStrategy(5, 20, 8.0, 18.0)
    assert(strategy.customersAt(7.9999) == 5)
    assert(strategy.customersAt(8.0) == 20)
    assert(strategy.customersAt(18.0) == 20)
    assert(strategy.customersAt(18.0001) == 5)

  test("StepStrategy should handle inverted time ranges"):
    val strategy = StepStrategy(5, 20, 18.0, 8.0)
    assert(strategy.customersAt(10.0) == 5)
    assert(strategy.customersAt(0.0) == 20)
    assert(strategy.customersAt(20.0) == 20)

  test("StepStrategy should handle zero-duration step"):
    val strategy = StepStrategy(1, 100, 5.0, 5.0)
    assert(strategy.customersAt(4.9) == 1)
    assert(strategy.customersAt(5.0) == 100)
    assert(strategy.customersAt(5.1) == 1)

  test("GaussianStrategy with zero stdDev should only return peak at mean"):
    val strategy = GaussianStrategy(100, 10.0, 0.0)
    assert(strategy.customersAt(10.0) == 100)
    assert(strategy.customersAt(10.1) == 0)
    assert(strategy.customersAt(9.9) == 0)

  test("StepStrategy with negative rates should return correct values"):
    val strategy = StepStrategy(-5, -2, 10.0, 20.0)
    assert(strategy.customersAt(5.0) == -5)
    assert(strategy.customersAt(15.0) == -2)
