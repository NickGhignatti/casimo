package model.entities.customers

import model.GlobalConfig
import model.entities.Movable
import model.managers.movements.Boids
import model.managers.{BaseManager, |}
import model.managers.movements.Boids.{AdapterManager, AlignmentManager, CohesionManager, MoverManager, PerceptionLimiterManager, SeparationManager, VelocityLimiterManager}
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
    val manager = AdapterManager(SeparationManager[Boid](
      avoidRadius = 10,
    ))
    val newBoids = manager.update(boids) pipe mover.update
    assert(distance(newBoids(0).position, newBoids(1).position) >
      distance(boids(0).position, boids(1).position))

  test("Two boids with only cohesion will get closer"):
    val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(100, 0)))
    val manager = AdapterManager(CohesionManager[Boid]())
    val newBoids = manager.update(boids) pipe mover.update
    assert(distance(newBoids(0).position, newBoids(1).position) <
      distance(boids(0).position, boids(1).position))

  test("Two boids with only alignment will align their directions"):
    val boids = Seq(Boid(Vector2D(0, 0), Vector2D(1, 0)), Boid(Vector2D(1, 0), Vector2D(0, 1)))
    val manager = AdapterManager(AlignmentManager[Boid]())
    val newBoids = manager.update(boids)
    assert((newBoids(0).direction dot newBoids(1).direction) >
      (boids(0).direction dot boids(1).direction))

  test("The boids velocity's magnitude cannot be bigger than a given parameter"):
    val boids = Seq(Boid(Vector2D(0, 0), Vector2D(1000, 0)))
    val manager = VelocityLimiterManager[Boid](
      maxSpeed = 10
    )
    val newBoids = manager.update(boids)
    assert(newBoids(0).direction.magnitude <= 10)

  private val boids = Seq(Boid(Vector2D(0, 0)), Boid(Vector2D(50, 0)), Boid(Vector2D(100, 0)))
  private val states = boids.map(Boids.State(_, boids))

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

  test("A boid can see all the others when the perception radius is large enough"):
    states.foreach: state =>
      assert((state | PerceptionLimiterManager(100)).others == boids)

  private case class BoidsManager(
                                            perceptionRadius: Double,
                                            avoidRadius: Double,
                                            maxSpeed: Double
                                          ) extends BaseManager[Seq[Boid]]:

    override def update(slice: Seq[Boid])(using config: GlobalConfig): Seq[Boid] =
      slice | Boids.AdapterManager(
        PerceptionLimiterManager(perceptionRadius)
          | AlignmentManager()
          | CohesionManager()
          | SeparationManager(avoidRadius)
      )
        | VelocityLimiterManager(maxSpeed)
        | MoverManager()

  test("Boids should move"):
    val manager = BoidsManager(
      perceptionRadius = 100,
      avoidRadius = 10,
      maxSpeed = 10
    )
    assert((boids | manager) != boids)