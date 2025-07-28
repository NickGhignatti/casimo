package model.entities.games

import model.entities.games.dsl.use
import org.scalatest.funsuite.AnyFunSuite
import utils.Result.Failure
import utils.Result.Success

class TestStrategies extends AnyFunSuite:

  test(
    "SlotStrategy.use should throw error when bet is negative"
  ):
    val bankRoll = 10.0
    val error = intercept[IllegalArgumentException](
      use(SlotStrategy) bet -5.0 when (bankRoll > 0.0)
    )

    assert(
      error.getMessage === "requirement failed: Bet amount must be positive"
    )

  test("SlotStrategy should return Failure when condition is false"):
    val bankRoll = 10.0
    val strategy = use(SlotStrategy) bet 5.0 when (bankRoll <= 0.0)

    strategy.use() match
      case Failure(_) => // Expected failure
      case Success(_) => fail("Expected Failure when condition is false")

  test("SlotStrategy should have default parameters when instatiace empty"):
    val bankRoll = 10.0
    val strategy = use(SlotStrategy) when (bankRoll > 0.0)

    assert(strategy.betAmount == 0.5)

  test("RouletteStrategy should throw error when bet is negative"):
    val bankRoll = 10.0
    val error = intercept[IllegalArgumentException](
      use(RouletteStrategy) bet -5.0 on List(0, 1, 2) when (bankRoll > 0.0)
    )

    assert(
      error.getMessage === "requirement failed: Bet amount must be positive"
    )

  test("RouletteStrategy should throw error when targets are empty"):
    val bankRoll = 10.0
    val error = intercept[IllegalArgumentException](
      use(RouletteStrategy) bet 5.0 on List.empty when (bankRoll > 0.0)
    )

    assert(
      error.getMessage === "requirement failed: Roulette bet must have at least one target"
    )

  test("RouletteStrategy should have default stuff when instantiated empty"):
    val bankRoll = 10.0
    val strategy = use(RouletteStrategy) when (bankRoll > 0.0)

    assert(strategy.targets == List(0) && strategy.betAmount == 0.5)

  test("RouletteStrategy should not throw error if instantiated correctly"):
    val bankRoll = 10.0
    val strategy = use(RouletteStrategy) when (bankRoll > 0.0)

    val result = strategy.use()

  test("BlackJackStrategy should throw error when bet is negative"):
    val bankRoll = 10.0
    val error = intercept[IllegalArgumentException](
      use(BlackJackStrategy) bet -5.0 accept 18 when (bankRoll > 0.0)
    )

    assert(
      error.getMessage === "requirement failed: Bet amount must be positive"
    )

  test(
    "BlackJackStrategy should have default parameters when instantiated empty"
  ):
    val bankRoll = 10.0
    val strategy = use(BlackJackStrategy) when (bankRoll > 0.0)

    assert(strategy.minimumValue == 17 && strategy.betAmount == 0.5)

  test("BlackJackStrategy should not throw error if instantiated correctly"):
    val bankRoll = 10.0
    val strategy = use(BlackJackStrategy) accept 17 when (bankRoll > 0.0)

    val result = strategy.use()
