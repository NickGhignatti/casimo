package model.entities.customers

import model.entities.games.GameBuilder
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestPreviousPosition extends AnyFunSuite:
  private val customer = Customer()
  private val game = GameBuilder.slot(Vector2D(10, 10))
  test("The customer previous position is initially none"):
    assert(customer.previousPosition.isEmpty)

  test(
    "When a customer sits to a game its previous position contains its previous position"
  ):
    val playingCustomer = customer.play(game)
    assert(playingCustomer.previousPosition.get == customer.position)
