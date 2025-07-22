package model.entities.games

import model.entities.customers.CustState
import model.entities.customers.CustState.Playing
import model.entities.customers.Customer
import model.entities.customers.RiskProfile.Regular
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestGameResolver extends AnyFunSuite:
  test("game resolver should return a list of updated games with 1 element"):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer().withCustomerState(Playing(mockGame))

    val newGame = GameResolver.update(List(mockCustomer), List(mockGame))

    assert(mockGame.gameHistory.gains != newGame.last.gameHistory.gains)

  test(
    "game resolver should return a list of updated games with 2 element also if customer is playing only a game"
  ):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockGame2 = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer().withCustomerState(Playing(mockGame))
    val newGame =
      GameResolver.update(List(mockCustomer), List(mockGame, mockGame2))

    assert(
      mockGame.gameHistory.gains :+ mockGame2.gameHistory.gains != newGame.last.gameHistory.gains
    )
