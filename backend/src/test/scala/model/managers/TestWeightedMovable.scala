package model.managers

import model.{GlobalConfig, given_GlobalConfig}
import org.scalatest.funsuite.AnyFunSuite
import model.managers.WeightedMovable.*

class TestWeightedMovable extends AnyFunSuite:
  test("WeightedMovable should apply weight to direction"):
    import model.entities.customers.Movable
    import utils.Vector2D

    case class TestMovable(position: Vector2D, direction: Vector2D) extends Movable[TestMovable]:
      override def updatedPosition(newPosition: Vector2D): TestMovable =
        this.copy(position = newPosition)
      override def updatedDirection(newDirection: Vector2D): TestMovable =
        this.copy(direction = direction + newDirection)

    case class Manager() extends BaseManager[Movable[TestMovable]]:
      override def update(slice: Movable[TestMovable])(using config: GlobalConfig): Movable[TestMovable] =
        slice.updatedDirection(Vector2D(1, 0))

    val movable = TestMovable(Vector2D(0, 0), Vector2D(1, 0))

    assert((movable |(2 * Manager())).direction == Vector2D(3, 0))
