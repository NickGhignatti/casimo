package model.entities.games

import model.entities.customers.CustState
import model.entities.customers.Customer
import model.entities.customers.RiskProfile.Regular
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestGameResolver extends AnyFunSuite:
  test("game resolver should return a list of updated games with 1 element"):
    val mockGame = GameBuilder.slot(Vector2D.zero)
    val mockCustomer = Customer(
      "id1",
      Vector2D.zero,
      Vector2D.zero,
      0.0,
      Regular,
      CustState.Playing(mockGame),
      
    )
    val newGame = GameResolver.update(List(mockCustomer), List(mockGame))

    assert(mockGame.gameHistory.gains != newGame.last.gameHistory.gains)
