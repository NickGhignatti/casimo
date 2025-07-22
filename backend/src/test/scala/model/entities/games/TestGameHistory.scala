package model.entities.games

import org.scalatest.funsuite.AnyFunSuite

class TestGameHistory extends AnyFunSuite:
  test("game history should increase when something is added"):
    val gameHistory = GameHistory(List.empty)
    assert(gameHistory.gains == List.empty)

    val newGameHistory = gameHistory.update("test", 10.0)
    assert(newGameHistory.gains.size == 1)

  test("game history should be consistent when more gains are added"):
    val gameHistory = GameHistory(List.empty)
    assert(gameHistory.gains == List.empty)

    val gh1 = gameHistory.update("test", 10.0)
    val gh2 = gh1.update("test", 20.0)
    val finalGH = gh2.update("test", 30.0)

    assert(finalGH.gains.map(_.getMoneyGain) == List(10.0, 20.0, 30.0))
