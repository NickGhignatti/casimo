package model.spawner

import model.entities.spawner.GaussianStrategy
import model.entities.spawner.SpawningStrategyBuilder
import org.scalatest.funsuite.AnyFunSuite

class TestDSLSpawningStrategy extends AnyFunSuite:

  test("Builder should create custom strategy from function"):
    val strategy = SpawningStrategyBuilder()
      .custom(time => (math.sin(time) * 10 + 15).toInt)
      .build()

    assert(strategy.customersAt(0) == 15)
    assert(strategy.customersAt(math.Pi / 2) == 25)
    assert(strategy.customersAt(math.Pi) == 15)
    assert(strategy.customersAt(3 * math.Pi / 2) == 5)

  test("Builder should apply offset to Gaussian strategy"):
    val strategy = SpawningStrategyBuilder()
      .gaussian(100, 10, 2)
      .offset(20)
      .build()

    assert(strategy.customersAt(10) == 120)
    assert(strategy.customersAt(0) == 20)
    assert(strategy.customersAt(20) == 20)

  test("Builder should scale Step strategy"):
    val strategy = SpawningStrategyBuilder()
      .step(5, 20, 8, 18)
      .scale(1.5)
      .build()

    assert(strategy.customersAt(0) == 8)
    assert(strategy.customersAt(10) == 30)
    assert(strategy.customersAt(20) == 8)

  test("Builder should clamp Gaussian strategy"):
    val strategy = SpawningStrategyBuilder()
      .gaussian(100, 10, 2)
      .clamp(min = 10, max = 80)
      .build()

    assert(strategy.customersAt(10) == 80)
    assert(strategy.customersAt(12) > 10)
    assert(strategy.customersAt(20) == 10)

  test("Builder should compose operations on Step strategy"):
    val strategy = SpawningStrategyBuilder()
      .step(10, 50, 9, 17)
      .offset(5)
      .scale(0.8)
      .clamp(15, 40)
      .build()

    assert(strategy.customersAt(0) == 15)
    assert(strategy.customersAt(12) == 40)
    assert(strategy.customersAt(18) == 15)

  test("Implicit DSL should allow arithmetic operations on strategies"):
    import SpawningStrategyBuilder.StrategyWrapper

    val base = SpawningStrategyBuilder().constant(10).build()
    val scaled = base * 1.5
    val offset = scaled + 5

    assert(offset.customersAt(0) == 20)
    assert(offset.customersAt(100) == 20)

    val gaussian = GaussianStrategy(100, 10, 2)
    val modified = (gaussian * 0.5) + 10

    assert(modified.customersAt(10) == 60)
    assert(modified.customersAt(20) == 10)

  test("Builder should handle empty custom strategy"):
    val strategy = SpawningStrategyBuilder()
      .custom(_ => 0)
      .offset(10)
      .build()

    assert(strategy.customersAt(0) == 10)
    assert(strategy.customersAt(100) == 10)

  test("Builder should not apply negative scaling"):
    val ex = intercept[IllegalArgumentException] {
      val strategy = SpawningStrategyBuilder()
        .constant(20)
        .scale(-1.0)
        .build()
    }

    assert(ex.getMessage === "requirement failed: scale factor should be >= 0")

  test("Clamp should not work with negative values"):
    val ex = intercept[IllegalArgumentException] {
      val strategy = SpawningStrategyBuilder()
        .custom(t => (t - 5).toInt)
        .clamp(min = -10, max = 10)
        .build()
    }

    assert(ex.getMessage === "requirement failed: minimum value should be >= 0")

  test("Clamp should not work with maximum value greater than minimum"):
    val ex = intercept[IllegalArgumentException] {
      val strategy = SpawningStrategyBuilder()
        .custom(t => (t - 5).toInt)
        .clamp(min = 20, max = 10)
        .build()
    }

    assert(
      ex.getMessage === "requirement failed: maximum value should be greater than minimum"
    )
