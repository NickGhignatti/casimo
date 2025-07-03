package model.customers

import model.SimulationState
import model.entities.games.Game
import utils.Vector2D
import utils.Vector2D.distance

object Boid:

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
    cohesionWeight = 0.3,
    separationWeight = 1
  )

case class Boid(
    position: Vector2D,
    velocity: Vector2D = Vector2D.Zero,
    parameters: Boid.Parameters = defaultParams,
    id: String = java.util.UUID.randomUUID().toString
) extends Customer:
  def update(simulationState: SimulationState): SimulationState =
    val nearbyPositions = nearbyBoids(simulationState.customers).map(_.position)
    val nearbyVelocities =
      nearbyBoids(simulationState.customers).map(_.velocity)

    val alignmentForce =
      parameters.alignmentWeight * alignment(nearbyVelocities)
    val cohesionForce = parameters.cohesionWeight * cohesion(nearbyPositions)
    val separationForce =
      parameters.separationWeight * separation(nearbyPositions)

    val newVelocity =
      (velocity + alignmentForce + cohesionForce + separationForce)
        .capped(parameters.maxSpeed)
    val newThis =
      this.copy(position = position + newVelocity, velocity = newVelocity)
    simulationState.copy(
      customers = simulationState.customers.map(customer =>
        if customer.id == this.id then newThis
        else customer
      )
    )

  extension (v: Vector2D)
    private def capped(max: Double): Vector2D =
      val mag = v.magnitude
      if mag > max then max * v.normalize else v

  private def nearbyBoids(boids: Seq[Customer]): Seq[Customer] =
    boids.filter(boid =>
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
