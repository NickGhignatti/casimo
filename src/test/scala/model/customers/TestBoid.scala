//package model.customers
//
//
//import org.scalatest.funsuite.AnyFunSuite
//import utils.Vector2D
//import model.customers.Boid.Context
//import utils.Vector2D.distance
//
//class TestBoid extends AnyFunSuite {
//
//  private val params = Boid.Parameters(
//    maxSpeed = 2,
//    perceptionRadius = 5,
//    avoidRadius = 1,
//    alignmentWeight = 1,
//    cohesionWeight = 1,
//    separationWeight = 1
//  )
//
//  private case class ContextImpl(
//                                  boids: Seq[Boid],
//                                  boid: Boid,
//                                  temporaryVelocity: Vector2D = Vector2D(0, 0)
//                                ) extends Context[ContextImpl] {
//    override def boidUpdated(boid: Boid): ContextImpl = this.copy(boid = boid)
//
//    override def updatedTemporaryVelocity(newVelocity: Vector2D): ContextImpl =
//      this.copy(temporaryVelocity = newVelocity)
//  }
//
//  test("update should not change velocity if no nearby boids are within perception radius") {
//    val boid = Boid(Vector2D(0, 0), Vector2D(1, 1), params.copy(maxSpeed = 10))
//    val otherBoid = Boid(Vector2D(10, 10), Vector2D(0, 0), params)
//    val context = ContextImpl(
//      Seq(boid, otherBoid)
//    )
//
//    val updatedBoid = boid.update(context)
//
//    assert(updatedBoid.velocity == boid.velocity)
//  }
//
//  test("update should cap velocity to maxSpeed") {
//    val boid = Boid(Vector2D(0, 0), Vector2D(5, 5), params)
//    val nearbyBoid = Boid(Vector2D(2, 2), Vector2D(1, 0), params)
//    val context = ContextImpl(Seq(boid, nearbyBoid))
//
//    val updatedBoid = boid.update(context)
//
//    assert(updatedBoid.velocity.magnitude <= params.maxSpeed)
//  }
//
//  test("two boids should be closer when only cohesion is applied") {
//    val boid1 = Boid(Vector2D(0, 0), parameters=params.copy(
//      alignmentWeight = 0,
//      cohesionWeight = 1,
//      separationWeight = 0
//    ))
//    val boid2 = Boid(Vector2D(3, 3), parameters=params)
//    val context = ContextImpl(Seq(boid1, boid2))
//
//    val updatedBoid1 = boid1.update(context)
//
//    assert(distance(updatedBoid1.position, boid2.position) < distance(boid1.position, boid2.position))
//  }
//
//  test("two boids should align when only alignment is applied") {
//    val alignOnly = params.copy(
//      alignmentWeight = 1,
//      cohesionWeight = 0,
//      separationWeight = 0
//    )
//    val boids = Seq(
//      Boid(Vector2D(0, 0), Vector2D(1, 0), parameters = alignOnly),
//      Boid(Vector2D(1, 1), Vector2D(0, 1), parameters = alignOnly)
//    )
//    val context = ContextImpl(boids)
//    val newBoids = boids.map(_.update(context))
//    assert((newBoids(0).velocity dot newBoids(1).velocity) > 0)
//  }
//
//  test("two boids should separate when only separation is applied") {
//    val separationOnly = params.copy(
//      alignmentWeight = 0,
//      cohesionWeight = 0,
//      separationWeight = 1,
//      avoidRadius = 1
//    )
//    val boid1 = Boid(Vector2D(0, 0), parameters = separationOnly)
//    val boid2 = Boid(Vector2D(0.5, 0.5), parameters = separationOnly)
//    val context = ContextImpl(Seq(boid1, boid2))
//
//    val updatedBoid1 = boid1.update(context)
//
//    assert(distance(updatedBoid1.position, boid2.position) > distance(boid1.position, boid2.position))
//  }
//}