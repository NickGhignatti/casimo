package model.entities.games

import org.scalatest.funsuite.AnyFunSuite

class TestBet extends AnyFunSuite:

  test("creating a positive FixedBet should store the amount"):
    val bet = FixedBet(100.0)
    assert(bet.amount === 100.0)

  test("creating a negative FixedBet should throw IllegalArgumentException"):
    val ex = intercept[IllegalArgumentException] {
      FixedBet(-50.0)
    }

    assert(ex.getMessage === "requirement failed: Bet amount must be positive")

  test("creating a correct RouletteBet should set amount and targets"):
    val bet = RouletteBet(50.0, List(0, 1, 2))
    assert(bet.amount === 50.0)
    assert(bet.targets === List(0, 1, 2))

  test(
    "creating a RouletteBet with negative amount should throw IllegalArgumentException"
  ):
    val ex = intercept[IllegalArgumentException] {
      RouletteBet(-10.0, List(0, 1, 2))
    }

    assert(ex.getMessage === "requirement failed: Bet amount must be positive")

  test(
    "creating a RouletteBet with empty targets should throw IllegalArgumentException"
  ):
    val ex = intercept[IllegalArgumentException] {
      RouletteBet(50.0, List.empty)
    }

    assert(
      ex.getMessage === "requirement failed: Roulette bet must have at least one target"
    )
