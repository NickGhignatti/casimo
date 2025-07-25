package model.managers.movements

import model.entities.customers.Movable
import model.managers.BaseManager
import model.managers.WeightedManager
import utils.Vector2D
import utils.Vector2D.distance

/** This objects contains the various managers which implements a boid-like
  * behaviour
  */
object Boids:
  case class Context[M <: Movable[M]](boid: M, others: Seq[M])
      extends Movable[Context[M]]:
    override def withPosition(newPosition: Vector2D): Context[M] =
      this.copy(boid = boid.withPosition(newPosition))

    override def withDirection(newDirection: Vector2D): Context[M] =
      this.copy(boid = boid.withDirection(newDirection))

    def directionAdded(addingDirection: Vector2D): Context[M] =
      this.copy(boid = boid.withDirection(boid.direction + addingDirection))

    export boid.{position, direction}

  /** This manager actually change the Movable position by adding to it its
    * direction
    */
  case class MoverManager[M <: Movable[M]]() extends BaseManager[M]:

    override def update(slice: M): M =
      slice.withPosition(slice.position + slice.direction)

  /** This manager implements the boid separation logic
    * @param avoidRadius
    *   the distance that each boid try to keep from the other boids
    */
  case class SeparationManager[M <: Movable[M]](
      avoidRadius: Double,
      weight: Double = 1
  ) extends WeightedManager[Context[M]]:

    override def update(slice: Context[M]): Context[M] =
      slice.directionAdded(
        separation(slice.boid, slice.others.map(_.position)) * weight
      )

    private def separation(boid: M, positions: Seq[Vector2D]): Vector2D =
      positions
        .filter(distance(boid.position, _) < avoidRadius)
        .map(pos => (boid.position - pos).normalize)
        .reduceOption(_ + _)
        .getOrElse(Vector2D.zero)

    override def updatedWeight(weight: Double): WeightedManager[Context[M]] =
      this.copy(weight = weight)

  /** This manager implement the boid cohesion logic
    */
  case class CohesionManager[M <: Movable[M]](weight: Double = 1)
      extends WeightedManager[Context[M]]:
    override def update(slice: Context[M]): Context[M] =
      slice.directionAdded(
        cohesion(
          slice.boid,
          slice.others.map(_.position)
        ) * weight
      )

    private def cohesion(boid: M, positions: Seq[Vector2D]): Vector2D =
      if positions.isEmpty then Vector2D.zero
      else
        val center = positions.reduce(_ + _) / positions.size
        (center - boid.position).normalize

    override def updatedWeight(weight: Double): WeightedManager[Context[M]] =
      this.copy(weight = weight)

  /** This manager implement the boid alignment logic
    */
  case class AlignmentManager[M <: Movable[M]](weight: Double = 1)
      extends WeightedManager[Context[M]]:
    override def update(slice: Context[M]): Context[M] =
      slice.directionAdded(
        alignment(
          slice.boid,
          slice.others.map(_.direction)
        ) * weight
      )

    private def alignment(boid: M, velocities: Seq[Vector2D]): Vector2D =
      if velocities.isEmpty then Vector2D.zero
      else
        val average = velocities.reduce(_ + _) / velocities.size
        (average - boid.direction).normalize

    override def updatedWeight(weight: Double): WeightedManager[Context[M]] =
      this.copy(weight = weight)

  /** This manager limits the velocity of the given boid
    * @param maxSpeed
    *   the maximum velocity that a boid can have
    */
  case class VelocityLimiterManager[M <: Movable[M]](maxSpeed: Double)
      extends BaseManager[M]:
    extension (v: Vector2D)
      private def capped(max: Double): Vector2D =
        if v.magnitude < max then v else v.normalize * max

    override def update(slice: M): M =
      slice.withDirection(slice.direction.capped(maxSpeed))

  /** This manager limits the number of surrounding boids the boid is influenced
    * by
    * @param perceptionRadius
    *   the radius within other boids influences this boid
    */
  case class PerceptionLimiterManager[M <: Movable[M]](perceptionRadius: Double)
      extends BaseManager[Context[M]]:
    override def update(slice: Context[M]): Context[M] =
      slice.copy(others =
        slice.others.filter(other =>
          distance(slice.boid.position, other.position) <= perceptionRadius
        )
      )
