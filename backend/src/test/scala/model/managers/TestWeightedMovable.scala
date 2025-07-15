package model.managers

import model.entities.customers.Movable
import model.given_GlobalConfig
import model.managers.WeightedManager._
import model.managers.movements.Boids
import model.managers.movements.Boids._
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestWeightedMovable extends AnyFunSuite:
  private case class TestMovable(position: Vector2D, direction: Vector2D)
      extends Movable[TestMovable]:
    override def updatedPosition(newPosition: Vector2D): TestMovable =
      this.copy(position = newPosition)
    override def updatedDirection(newDirection: Vector2D): TestMovable =
      this.copy(direction = newDirection)

  test("Weighting should work with cohesion, alignment and separation"):
    val boid = TestMovable(Vector2D(0, 0), Vector2D(0, 0))
    val state =
      Boids.State(boid, Seq(boid, TestMovable(Vector2D(1, 0), Vector2D(0, 1))))
    Seq(
      CohesionManager[TestMovable](),
      AlignmentManager[TestMovable](),
      SeparationManager[TestMovable](Double.PositiveInfinity)
    )
      .foreach { manager =>
        assert(
          (state | 2 * manager).direction == (state | manager).direction * 2,
          manager.getClass.getName
        )
      }
