package model.entities.customers

import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import model.managers.|
import model.given_GlobalConfig

class DefaultMovementManagerTest extends AnyFunSuite:
  test("DefaultMovementManager should move the customers"):
    val customers = Seq(
      Customer(
        id = "Alice",
        position = Vector2D(0, 0),
        direction = Vector2D(1, 0),
        bankroll = 100.0,
      ),
      Customer(
        id = "Bob",
        position = Vector2D(10, 10),
        direction = Vector2D(0, 1),
        bankroll = 100.0,
      )
    )
    assert((customers | DefaultMovementManager()) != customers)
