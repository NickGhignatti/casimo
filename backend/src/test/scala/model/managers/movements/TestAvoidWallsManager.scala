package model.managers.movements

import model.entities.Collidable
import model.entities.customers.Movable
import model.managers.movements.AvoidWallsManager.Context
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestAvoidWallsManager extends AnyFunSuite:
  private case class MockMovable(direction: Vector2D, position: Vector2D)
      extends Movable[MockMovable]:

    override def withPosition(newPosition: Vector2D): MockMovable =
      this.copy(position = newPosition)

    override def withDirection(newDirection: Vector2D): MockMovable =
      this.copy(direction = newDirection)

  private case class Wall(position: Vector2D, width: Double, height: Double)
      extends Collidable

  test(
    "A movable which is close to a wall should not collide with the wall"
  ):
    val movable = MockMovable(
      position = Vector2D(5, 0),
      direction = Vector2D(-3, 0)
    )
    val wall = Wall(Vector2D(0, 10), 1, 5)
    val walls = Seq(wall)
    val context = Context(movable, walls)
    val updatedContext = context | AvoidWallsManager()
    assert(updatedContext.movable.position.x > wall.position.x + 1)
