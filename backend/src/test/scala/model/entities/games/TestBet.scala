package model.entities.games

import org.scalatest.funsuite.AnyFunSuite

class TestBet extends AnyFunSuite:

  test("creating a positive SlotBet should store the amount"):
    val bet = SlotBet(100.0)
    assert(bet.amount === 100.0)

  test("creating a negative SlotBet should throw IllegalArgumentException"):
    val ex = intercept[IllegalArgumentException] {
      SlotBet(-50.0)
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

  test("creating a correct BlackJackBet should set amount and minimum value"):
    val bet = BlackJackBet(50.0, 18)
    assert(bet.amount === 50.0)
    assert(bet.minimumValue === 18)

  test(
    "creating a BlackJackBet with negative amount should throw IllegalArgumentException"
  ):
    val ex = intercept[IllegalArgumentException] {
      BlackJackBet(-10.0, 10)
    }

    assert(ex.getMessage === "requirement failed: Bet amount must be positive")

  test(
    "creating a BlackJackBet with empty targets should throw IllegalArgumentException"
  ):
    val ex = intercept[IllegalArgumentException] {
      BlackJackBet(50.0, -10)
    }

    assert(
      ex.getMessage === "requirement failed: Cards value must be positive"
    )
