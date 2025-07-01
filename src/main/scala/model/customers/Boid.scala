package model.customers

import model.SimulationState
import model.entities.games.Game
import utils.Vector2D
import utils.Vector2D.distance

object Boid:
  trait Context:
    def boids: Seq[Boid]

  case class Parameters(
      maxSpeed: Double,
      perceptionRadius: Double,
      avoidRadius: Double,
      alignmentWeight: Double,
      cohesionWeight: Double,
      separationWeight: Double
  )

  private val defaultParams = Parameters(
    maxSpeed = 2,
    perceptionRadius = 1000,
    avoidRadius = 1,
    alignmentWeight = 1,
    cohesionWeight = 1,
    separationWeight = 1
  )

case class Boid(
    position: Vector2D,
    velocity: Vector2D = Vector2D.Zero,
    parameters: Boid.Parameters = defaultParams
):
  import Boid.Context
  def update(context: Context): Boid =
    given Context = context
    val nearbyPositions = nearbyBoids.map(_.position)
    val nearbyVelocities = nearbyBoids.map(_.velocity)

    val alignmentForce =
      parameters.alignmentWeight * alignment(nearbyVelocities)
    val cohesionForce = parameters.cohesionWeight * cohesion(nearbyPositions)
    val separationForce =
      parameters.separationWeight * separation(nearbyPositions)

    val newVelocity =
      (velocity + alignmentForce + cohesionForce + separationForce)
        .capped(parameters.maxSpeed)
    this.copy(position = position + newVelocity, velocity = newVelocity)

  extension (v: Vector2D)
    private def capped(max: Double): Vector2D =
      val mag = v.magnitude
      if mag > max then max * v.normalize else v

  private def nearbyBoids(using context: Context): Seq[Boid] =
    context.boids.filter(boid =>
      distance(boid.position, position) <= parameters.perceptionRadius
    )

  private def alignment(velocities: Seq[Vector2D]): Vector2D =
    if velocities.isEmpty then Vector2D.Zero
    else
      val average = velocities.reduce(_ + _) / velocities.size
      (average - velocity).normalize

  private def cohesion(positions: Seq[Vector2D]): Vector2D =
    if positions.isEmpty then Vector2D.Zero
    else
      val center = positions.reduce(_ + _) / positions.size
      (center - position).normalize

  private def separation(positions: Seq[Vector2D]): Vector2D =
    if positions.isEmpty then Vector2D.Zero
    else
      positions
        .filter(distance(position, _) < parameters.avoidRadius)
        .map(pos => (position - pos).normalize)
        .reduce(_ + _)
