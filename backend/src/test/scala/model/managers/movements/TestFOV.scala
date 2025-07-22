package model.managers.movements

import model.entities.Collidable
import model.managers.movements.FOV.canSee
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestFOV extends AnyFunSuite:
  private case class Wall(position: Vector2D, width: Double, height: Double)
      extends Collidable

  test("A collidable should hide a point behind it"):
    val pointOfView = Vector2D(0, 0)
    val collidable = Wall(Vector2D(1, 0), 2, 2)
    val pointBehind = Vector2D(2, 0)
    assert(!pointOfView.canSee(Seq(collidable))(pointBehind))

  test("A point can be seen is there's no obstacles"):
    val pointOfView = Vector2D(0, 0)
    val pointToSee = Vector2D(2, 0)
    assert(pointOfView.canSee(Seq.empty)(pointToSee))

  test("A point can be seen if it's not behind a wall"):
    val pointOfView = Vector2D(0, 0)
    val collidable = Wall(Vector2D(1, 0), 2, 2)
    val pointToSee = Vector2D(0, 3)
    assert(pointOfView.canSee(Seq(collidable))(pointToSee))

  test("A point cannot be seen if it's surrounded by walls"):
    val obstacles = Seq(
      Wall(Vector2D(-2, -2), 5, 1), // bottom
      Wall(Vector2D(-2, 2), 5, 1), // top
      Wall(Vector2D(-2, -2), 1, 5) // left
    )
    val pointToSee = Vector2D(0, 0)
    assert(!Vector2D(-10, 0).canSee(obstacles)(pointToSee))
    assert(!Vector2D(0, 10).canSee(obstacles)(pointToSee))
    assert(!Vector2D(0, -10).canSee(obstacles)(pointToSee))
    assert(Vector2D(10, 0).canSee(obstacles)(pointToSee))
    assert(
      !Vector2D(10, 0).canSee(
        obstacles :+ Wall(Vector2D(2, -2), 1, 5) // right
      )(pointToSee)
    )
