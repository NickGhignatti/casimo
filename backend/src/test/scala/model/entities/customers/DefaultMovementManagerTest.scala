package model.entities.customers

import model.entities.games.GameType
import model.entities.{CustState, RiskProfile}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import model.managers.|
import model.given_GlobalConfig

class DefaultMovementManagerTest extends AnyFunSuite:
  test("DefaultMovementManager should not diverge"):
    val customers = Seq(
      Customer(
        id = "Alice",
        position = Vector2D(0, 0),
        bankroll = 100.0,
      ),
      Customer(
        id = "Bob",
        position = Vector2D(10, 10),
        bankroll = 100.0,
      )
    )
    assert((customers | DefaultMovementManager()) != customers)
