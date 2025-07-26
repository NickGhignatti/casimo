package model.managers.movements

import model.entities.customers.Movable
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestRandomMovementTest extends AnyFunSuite:
  private case class MovableImpl(position: Vector2D, direction: Vector2D)
      extends Movable[MovableImpl]:
    override def withPosition(newPosition: Vector2D): MovableImpl =
      copy(position = newPosition)

    override def withDirection(newDirection: Vector2D): MovableImpl =
      copy(direction = newDirection)

  test("A movable moved by a brownian motion should move"):
    val movable = MovableImpl(Vector2D.zero, Vector2D.zero)
    val updated = movable | RandomMovementManager()
    assert(updated.direction.magnitude > 0)
