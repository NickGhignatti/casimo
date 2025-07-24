package model.managers.movements

import model.SimulationState
import model.entities.customers.CustState.Playing
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import model.entities.games.GameBuilder
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class DefaultMovementManagerTest extends AnyFunSuite:
  test("DefaultMovementManager should move the customers"):
    val simulationState = SimulationState(
      customers = Seq(
        Customer(
          id = "Alice",
          position = Vector2D(0, 0),
          direction = Vector2D(1, 0),
          bankroll = 100.0
        ),
        Customer(
          id = "Bob",
          position = Vector2D(10, 10),
          direction = Vector2D(0, 1),
          bankroll = 100.0
        )
      ),
      games = List.empty,
      spawner = None,
      walls = List.empty
    )
    assert(
      (simulationState | DefaultMovementManager()).customers
        .map(_.position) != simulationState.customers.map(_.position)
    )

  test(
    "DefaultMovementManager should not change the customers which are currently playing"
  ):
    val simulationState = SimulationState(
      customers = Seq(
        Customer(
          id = "Alice",
          position = Vector2D(0, 0),
          direction = Vector2D(0, 0),
          bankroll = 100.0,
          customerState = Playing(GameBuilder.blackjack(Vector2D.zero))
        ),
        Customer(
          id = "Bob",
          position = Vector2D(10, 10),
          direction = Vector2D(0, 1),
          bankroll = 100.0
        ),
        Customer(
          id = "Charlie",
          position = Vector2D(20, 20),
          direction = Vector2D(1, 0),
          bankroll = 100.0
        )
      ),
      games = List.empty,
      spawner = None,
      walls = List.empty
    )
    val updatedCustomers =
      (simulationState | DefaultMovementManager()).customers
    assert(updatedCustomers.head.position == Vector2D(0, 0))
    assert(updatedCustomers.head.direction == Vector2D(0, 0))
    assert(updatedCustomers.head.isPlaying)
    updatedCustomers
      .slice(1, updatedCustomers.length)
      .foreach(customer =>
        assert(customer.direction != Vector2D.zero)
        assert(!customer.isPlaying)
      )
