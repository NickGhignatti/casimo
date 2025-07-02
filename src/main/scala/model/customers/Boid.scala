package model.customers

import model.SimulationState
import model.entities.games.Game
import utils.Vector2D
import utils.Vector2D.distance

object GameLikeness:
  trait Context[C <: Context[C]]:
    def boid: Boid
    def temporaryVelocity: Vector2D
    def games: Seq[Game]
    def favouriteGames: Seq[Game]
    def boidUpdated(boid: Boid): C
    def updatedTemporaryVelocity(newVelocity: Vector2D): C
    def updatedFavouriteGames(games: Seq[Game]): C

  def gamesLikeness[C <: Context[C]](context: C): C =
    context.games
      .find(_ == context.favouriteGames.head)
      .map(game => direction(game.position, context.boid.position))
      .map(context.updatedTemporaryVelocity)
      .getOrElse(context)

  private def direction(first: Vector2D, second: Vector2D): Vector2D =
    (first - second).normalize

object Boid:
  trait Context[C <: Context[C]]:
    def boid: Boid
    def temporaryVelocity: Vector2D
    def boids: Seq[Boid]
    def boidUpdated(boid: Boid): C
    def updatedTemporaryVelocity(newVelocity: Vector2D): C

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

  def alignment[C <: Context[C]](context: C): C =
    val nearbyVelocities =
      context.boid.nearbyBoids(context.boids).map(_.velocity)
    val alignmentForce =
      context.boid.parameters.alignmentWeight * context.boid.alignment(
        nearbyVelocities
      )
    context.updatedTemporaryVelocity(context.temporaryVelocity + alignmentForce)

case class Boid(
    position: Vector2D,
    velocity: Vector2D = Vector2D.Zero,
    parameters: Boid.Parameters = defaultParams
):
  import Boid.Context
  def update(context: Context[_]): Boid =
    given Context[_] = context
    val nearbyPositions = nearbyBoids(context.boids).map(_.position)
    val nearbyVelocities = nearbyBoids(context.boids).map(_.velocity)

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

  private def nearbyBoids(boids: Seq[Boid]): Seq[Boid] =
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
