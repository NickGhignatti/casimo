package model.entities.games

import model.entities.games.dsl.use
import org.scalatest.funsuite.AnyFunSuite
import utils.Result.{Failure, Success}

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

  test("BlackJackStrategy should throw error when bet is negative"):
    val bankRoll = 10.0
    val error = intercept[IllegalArgumentException](
      use(BlackJackStrategy) bet -5.0 accept 18 when (bankRoll > 0.0)
    )

    assert(
      error.getMessage === "requirement failed: Bet amount must be positive"
    )
