package model.managers.movements

import model.entities.customers.Movable
import model.managers.BaseManager
import model.managers.movements.Boids._
import model.managers.|
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D
import utils.Vector2D.distance

class TestBoids extends AnyFunSuite:

  private case class Boid(
      position: Vector2D,
      direction: Vector2D = Vector2D.zero
  ) extends Movable[Boid]:
    override def withPosition(newPosition: Vector2D): Boid =
      copy(position = newPosition)

    override def withDirection(newDirection: Vector2D): Boid =
      copy(direction = newDirection)

  case class AdapterManager[M <: Movable[M]](manager: BaseManager[State[M]])
      extends BaseManager[Seq[M]]:

    override def update(slice: Seq[M]): Seq[M] =
      slice
        .map(boid => State(boid, slice))
        .map(_ | manager)
        .map(_.boid)

  test("Two boids with only separation will increase their distance"):
    val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(1, 0)))
    val manager = AdapterManager(SeparationManager[Boid](10) | MoverManager())
    val newBoids = boids | manager
    assert(
      distance(newBoids(0).position, newBoids(1).position) >
        distance(boids(0).position, boids(1).position)
    )

  test("Two boids with only cohesion will get closer"):
    val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(100, 0)))
    val manager = AdapterManager(CohesionManager[Boid]() | MoverManager())
    val newBoids = boids | manager
    assert(
      distance(newBoids(0).position, newBoids(1).position) <
        distance(boids(0).position, boids(1).position)
    )

  test("Two boids with only alignment will align their directions"):
    val boids = Seq(
      Boid(Vector2D(0, 0), Vector2D(1, 0)),
      Boid(Vector2D(1, 0), Vector2D(0, 1))
    )
    val manager = AdapterManager(AlignmentManager[Boid]())
    val newBoids = boids | manager
    assert(
      (newBoids(0).direction dot newBoids(1).direction) >
        (boids(0).direction dot boids(1).direction)
    )

  test(
    "The boids velocity's magnitude cannot be bigger than a given parameter"
  ):
    val boid = Boid(Vector2D(0, 0), Vector2D(1000, 0))
    assert((boid | VelocityLimiterManager(10)).direction.magnitude <= 10)

  private val boids =
    Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(50, 0)), Boid(Vector2D(100, 0)))
  private val states = boids.map(State(_, boids))

  test("A boid can only see itself when its perception radius is 0"):
    states.foreach: state =>
      val others = (state | PerceptionLimiterManager(0)).others
      assert(others.size == 1)
      assert(others.head == state.boid)

  test("A boid can only see the others within its perception radius"):
    val limiter50 = PerceptionLimiterManager[Boid](50)
    assert((states(0) | limiter50).others == Seq(boids(0), boids(1)))
    assert((states(1) | limiter50).others == Seq(boids(0), boids(1), boids(2)))
    assert((states(2) | limiter50).others == Seq(boids(1), boids(2)))
