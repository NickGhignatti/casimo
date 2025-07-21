package model.entities.customers

import model.entities.Collidable
import model.managers.movements.AvoidWallsManager
import model.managers.movements.AvoidWallsManager.Context
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Rotation._
import utils.Vector2D

class TestAvoidWallsManager extends AnyFunSuite:
  private case class MockMovable(direction: Vector2D, position: Vector2D)
      extends Movable[MockMovable]:

    override def updatedPosition(newPosition: Vector2D): MockMovable =
      this.copy(position = newPosition)

    override def updatedDirection(newDirection: Vector2D): MockMovable =
      this.copy(direction = newDirection)

  private case class Wall(position: Vector2D, width: Double, height: Double)
      extends Collidable

  test(
    "A movable which is close to a wall should change its direction to avoid it"
  ):
    val movable = MockMovable(
      position = Vector2D(5, 0),
      direction = Vector2D(-3, 0)
    )
    val wall = Wall(Vector2D(0, 10), 1, 5)
    val walls = Seq(wall)
    val context = Context(movable, walls)
    val updatedContext = context | AvoidWallsManager(100)
    assert(updatedContext.movable.direction.x > movable.direction.x)
