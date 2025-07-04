package model.entities.customers

import model.GlobalConfig
import model.entities.Movable
import model.managers.movements.Boids.{CohesionManager, MoverManager, SeparationManager}
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import utils.Vector2D.distance

import scala.util.chaining.scalaUtilChainingOps

class TestBoids extends AnyFunSuite:

  private case class Boid(position: Vector2D, direction: Vector2D = Vector2D.zero) extends Movable[Boid]:
    override def updatedPosition(newPosition: Vector2D): Boid =
      copy(position = newPosition)

    override def updatedDirection(newDirection: Vector2D): Boid =
      copy(direction = newDirection)

  private val mover = MoverManager[Boid]()
  private given GlobalConfig = GlobalConfig()

  test("Two boids with only separation will increase their distance"):
    val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(1, 0)))
    val manager = SeparationManager[Boid](
      perceptionRadius = 1,
      avoidRadius = 10,
      cohesionWeight = 0,
      separationWeight = 1,
      alignmentWeight = 0
    )
    val newBoids = manager.update(boids) pipe mover.update
    assert(distance(newBoids(0).position, newBoids(1).position) >
      distance(boids(0).position, boids(1).position))

  test("Two boids with only cohesion will get closer"):
    val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(100, 0)))
    val manager = CohesionManager[Boid]()
    val newBoids = manager.update(boids) pipe mover.update
    assert(distance(newBoids(0).position, newBoids(1).position) <
      distance(boids(0).position, boids(1).position))